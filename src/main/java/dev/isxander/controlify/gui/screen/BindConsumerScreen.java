package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.controllers.BindController;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class BindConsumerScreen extends Screen implements ScreenProcessorProvider {
    private final BindConsumer bindConsumer;
    private final Option<Input> option;
    private final Screen backgroundScreen;
    private final BindController.BindControllerElement widgetToFocus;
    private final ScreenProcessorImpl screenProcessor = new ScreenProcessorImpl(this);

    private int ticksTillClose;
    private int ticksTillInput;

    public BindConsumerScreen(BindConsumer bindConsumer, Option<Input> option, BindController.BindControllerElement widgetToFocus, Screen backgroundScreen) {
        super(Component.empty());
        this.bindConsumer = bindConsumer;
        this.option = option;
        this.widgetToFocus = widgetToFocus;
        this.backgroundScreen = backgroundScreen;
        this.ticksTillInput = 5;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Dimension<Integer> dim = widgetToFocus.getDimension();

        backgroundScreen.extractRenderStateWithTooltipAndSubtitles(graphics, dim.centerX(), dim.centerY(), a);

        graphics.nextStratum();

        // darken everything except the widget
        graphics.fill(0, 0, width, dim.y() - 1, 0x80000000);
        graphics.fill(0, dim.y(), dim.x() - 1, height, 0x80000000);
        graphics.fill(dim.xLimit() + 1, dim.y() - 1, width, height, 0x80000000);
        graphics.fill(dim.x(), dim.yLimit() + 1, dim.xLimit(), height, 0x80000000);

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // do not render background
    }

    @Override
    public void tick() {
        if (ticksTillClose > 0) {
            ticksTillClose--;
            if (ticksTillClose == 0) {
                widgetToFocus.awaitingControllerInput = false;
                // don't call setScreen because will cause background to re-init
                minecraft.screen = backgroundScreen;
            }
        }

        if (ticksTillInput > 0) {
            ticksTillInput--;
            if (ticksTillInput > 0) {
                return;
            }
        }

        // tick runs after all controller input ticks

        Optional<Input> pressedBind = bindConsumer.getPressedBind();
        if (pressedBind.isPresent()) {
            option.requestSet(pressedBind.get());
            returnToBackground();
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        boolean consumed = super.keyPressed(keyEvent);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        boolean consumed = super.mouseClicked(mouseButtonEvent, doubleClick);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        boolean consumed = super.mouseDragged(mouseButtonEvent, dx, dy);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        boolean consumed = super.mouseScrolled(mouseX, mouseY, amount, d);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    private void returnToBackground() {
        ticksTillClose = 5;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }

    public interface BindConsumer {
        Optional<Input> getPressedBind();
    }

    private static class ScreenProcessorImpl extends ScreenProcessor<BindConsumerScreen> {
        public ScreenProcessorImpl(BindConsumerScreen screen) {
            super(screen);
        }

        @Override
        public void onControllerUpdate(ControllerEntity controller) {
            // prevent all other controller input logic
        }
    }
}
