package dev.isxander.controlify.screenkeyboard;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class KeyboardWidget extends AbstractWidget implements ContainerEventHandler {
    private final ResourceLocation currentLayout;
    private KeyboardInputConsumer inputConsumer;

    private List<KeyWidget> keys = ImmutableList.of();

    private boolean shifted, shiftLocked;

    private @Nullable KeyWidget focused;
    private boolean isDragging;

    private final Screen containingScreen;

    public KeyboardWidget(int x, int y, int width, int height, KeyboardLayoutWithId layout, KeyboardInputConsumer inputConsumer, Screen containingScreen) {
        super(x, y, width, height, Component.literal("On-Screen Keyboard"));
        this.inputConsumer = inputConsumer;
        this.containingScreen = containingScreen;
        this.currentLayout = layout.id();
        this.updateLayout(layout.layout(), "initial_focus", null);
    }

    public void updateLayout(KeyboardLayoutWithId layout) {
        ResourceLocation oldLayoutId = this.getCurrentLayoutId();
        @Nullable String oldIdentifier = Optional.ofNullable(getFocused())
                .map(k -> k.getKey().identifier())
                .orElse(null);

        this.updateLayout(layout.layout(), oldIdentifier, oldLayoutId);
    }

    public void updateLayout(KeyboardLayout layout, @Nullable String identifierToFocus, @Nullable ResourceLocation oldLayoutChangerToFocus) {
        this.arrangeKeys(layout);

        findKey(
                identifierToFocus != null,
                k -> Objects.equals(k.getKey().identifier(), identifierToFocus)
        ).or(() -> findKey(
                oldLayoutChangerToFocus != null,
                k -> k.getKey() instanceof KeyboardLayout.Key.ChangeLayoutKey changeLayoutKey && changeLayoutKey.otherLayout().equals(oldLayoutChangerToFocus))
        );
    }

    private void arrangeKeys(KeyboardLayout layout) {
        int keyCount = layout.keys().stream()
                .mapToInt(List::size)
                .sum();
        this.keys = new ArrayList<>(keyCount);

        float unitWidth = this.getWidth() / layout.width();
        float keyHeight = (float) this.getHeight() / layout.keys().size();

        float y = this.getY();
        for (List<KeyboardLayout.ShiftableKey> row : layout.keys()) {
            float x = this.getX();
            for (KeyboardLayout.ShiftableKey key : row) {
                float keyWidth = key.width() * unitWidth;

                var keyWidget = new KeyWidget(
                        (int) x, (int) y, (int) keyWidth, (int) keyHeight,
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

    public void setShifted(boolean shifted) {
        this.shifted = shifted;
    }

    public boolean isShifted() {
        return shifted;
    }

    public void setShiftLocked(boolean shiftLocked) {
        this.shiftLocked = shiftLocked;
    }

    public boolean isShiftLocked() {
        return shiftLocked;
    }

    public void setInputConsumer(KeyboardInputConsumer inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    public KeyboardInputConsumer getInputConsumer() {
        return inputConsumer;
    }

    public ResourceLocation getCurrentLayoutId() {
        return this.currentLayout;
    }

    private Optional<KeyWidget> findKey(boolean skipSearch, Predicate<KeyWidget> predicate) {
        if (skipSearch) {
            return Optional.empty();
        }

        return this.keys.stream()
                .filter(predicate)
                .findFirst();
    }

    @Override
    public @NotNull List<KeyWidget> children() {
        return Collections.unmodifiableList(keys);
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
    public @Nullable KeyWidget getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focused != null && (!(focused instanceof KeyWidget) || !this.keys.contains(focused))) {
            throw new IllegalArgumentException("Focused widget must be a KeyWidget in this KeyboardWidget");
        }

        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = (KeyWidget) focused;
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
