package dev.isxander.controlify.screenkeyboard;

import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KeyboardWidget extends AbstractWidget implements ContainerEventHandler {
    private final KeyboardLayout layout;
    final KeyboardInputConsumer inputConsumer;

    private final List<KeyWidget> keys;

    boolean shifting;

    private @Nullable GuiEventListener focused;
    private boolean isDragging;

    private final Screen containingScreen;

    public KeyboardWidget(int x, int y, int width, int height, KeyboardLayout layout, KeyboardInputConsumer inputConsumer, Screen containingScreen) {
        super(x, y, width, height, Component.literal("On-Screen Keyboard"));
        this.layout = layout;
        this.inputConsumer = inputConsumer;
        this.containingScreen = containingScreen;

        int keyCount = this.layout.keys().stream()
                .mapToInt(List::size)
                .sum();
        this.keys = new ArrayList<>(keyCount);
        this.arrangeKeys();
        System.out.println(layout);
    }

    private void arrangeKeys() {
        this.keys.clear();

        float unitWidth = (float) this.getWidth() / this.layout.rowWidth();
        int keyHeight = (int)((float) this.getHeight() / this.layout.keys().size());

        int y = this.getY();
        for (List<KeyboardLayout.ShiftableKey> row : this.layout.keys()) {
            int x = this.getX();
            for (KeyboardLayout.ShiftableKey key : row) {
                int keyWidth = (int) (key.regular().width() * unitWidth);

                var keyWidget = new KeyWidget(
                        x, y, keyWidth, keyHeight,
                        key, this
                );
                ScreenProcessorProvider.provide(this.containingScreen).addEventListener(keyWidget);

                this.keys.add(keyWidget);

                x += keyWidth;
            }
            y += keyHeight;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (KeyWidget key : keys) {
            // vanilla widget render does other stuff like mouse hover update etc
            // render method of keys are empty - this doesn't actually do any rendering
            key.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // draw in a managed context so we can batch render calls
        // everything within here is rendered in a single draw call
        Blit.batchDraw(guiGraphics, () -> {
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
            guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), 0xFFAAAAAA);

            for (KeyWidget key : keys) {
                // every key background is rendered into the same vertex buffer to upload at once
                key.renderKeyBackground(guiGraphics, mouseX, mouseY, partialTick);
            }

            // renders all foreground after background to prevent context switching
            for (KeyWidget key : keys) {
                // text rendering is batched by default in managed mode
                key.renderKeyForeground(guiGraphics, mouseX, mouseY, partialTick);
            }
        });
    }

    @Override
    public @NotNull List<KeyWidget> children() {
        return this.keys;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    @Override
    public @Nullable GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button /*? if >=1.21.9 {*/ ,boolean doubleClick /*?}*/) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button /*? if >=1.21.9 >>*/ ,doubleClick );
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        ContainerEventHandler.super.setFocused(focused);
    }

}
