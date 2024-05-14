package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.controllers.BindController;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BindConsumerScreen extends Screen implements ScreenProcessorProvider {
    private final BindConsumer bindConsumer;
    private final Option<Bind> option;
    private final Screen backgroundScreen;
    private final BindController.BindControllerElement widgetToFocus;
    private final ScreenProcessorImpl screenProcessor = new ScreenProcessorImpl(this);

    private int ticksTillClose;
    private int ticksTillInput;

    public BindConsumerScreen(BindConsumer bindConsumer, Option<Bind> option, BindController.BindControllerElement widgetToFocus, Screen backgroundScreen) {
        super(Component.empty());
        this.bindConsumer = bindConsumer;
        this.option = option;
        this.widgetToFocus = widgetToFocus;
        this.backgroundScreen = backgroundScreen;
        this.ticksTillInput = 5;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        Dimension<Integer> dim = widgetToFocus.getDimension();

        guiGraphics.pose().pushPose();
        // text renders with z > 0 so push everything back so text doesn't pop through fill
        guiGraphics.pose().translate(0, 0, -20);

        backgroundScreen.render(guiGraphics, dim.centerX(), dim.centerY(), tickDelta);

        guiGraphics.pose().popPose();

        // darken everything except the widget
        guiGraphics.fill(0, 0, width, dim.y() - 1, 0x80000000);
        guiGraphics.fill(0, dim.y(), dim.x() - 1, height, 0x80000000);
        guiGraphics.fill(dim.xLimit() + 1, dim.y() - 1, width, height, 0x80000000);
        guiGraphics.fill(dim.x(), dim.yLimit() + 1, dim.xLimit(), height, 0x80000000);

        super.render(guiGraphics, mouseX, mouseY, tickDelta);
    }

    @Override
    /*? if >=1.20.4 {*/
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    /*?} else {*//*
    public void renderBackground(GuiGraphics guiGraphics)
    *//*?} */
    {
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

        Optional<Bind> pressedBind = bindConsumer.getPressedBind();
        if (pressedBind.isPresent()) {
            option.requestSet(pressedBind.get());
            returnToBackground();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean consumed = super.keyPressed(keyCode, scanCode, modifiers);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean consumed = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (consumed) return true;

        if (ticksTillInput > 0) return false;
        returnToBackground();
        return true;
    }

    @Override
    /*? if >=1.20.4 {*/
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        boolean consumed = super.mouseScrolled(mouseX, mouseY, amount, d);
    /*?} else {*//*
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean consumed = super.mouseScrolled(mouseX, mouseY, amount);
    *//*?} */
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
        Optional<Bind> getPressedBind();
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
