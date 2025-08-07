package dev.isxander.controlify.screenkeyboard;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.screenop.ScreenProcessor;
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

public class KeyboardWidget extends AbstractWidget implements ContainerEventHandler, ComponentProcessor {
    private ResourceLocation currentLayout;
    private @Nullable ResourceLocation previousLayout;

    private InputTarget inputConsumer;

    private List<KeyWidget> keys = ImmutableList.of();

    private boolean shifted, shiftLocked;

    private @Nullable KeyWidget focused;
    private boolean isDragging;

    private final Screen containingScreen;

    public KeyboardWidget(int x, int y, int width, int height, KeyboardLayoutWithId layout, InputTarget inputConsumer, Screen containingScreen) {
        super(x, y, width, height, Component.literal("On-Screen Keyboard"));
        this.inputConsumer = inputConsumer;
        this.containingScreen = containingScreen;
        this.updateLayout(layout, "initial_focus", null);
    }

    public void updateLayout(KeyboardLayoutWithId layout) {
        ResourceLocation oldLayoutId = this.getCurrentLayoutId();
        @Nullable String oldIdentifier = Optional.ofNullable(getFocused())
                .map(k -> k.getKey().identifier())
                .orElse(null);

        this.updateLayout(layout, oldIdentifier, oldLayoutId);
    }

    public void updateLayout(KeyboardLayoutWithId layout, @Nullable String identifierToFocus, @Nullable ResourceLocation oldLayoutChangerToFocus) {
        this.previousLayout = this.currentLayout;
        this.currentLayout = layout.id();

        this.arrangeKeys(layout.layout());

        findKey(
                identifierToFocus != null,
                k -> Objects.equals(k.getKey().identifier(), identifierToFocus)
        ).or(() -> findKey(
                oldLayoutChangerToFocus != null,
                k -> {
                    boolean isOldLayout = k.getKeyFunction() instanceof KeyboardLayout.KeyFunction.ChangeLayoutFunc changeLayoutKey
                            && changeLayoutKey.layout().equals(oldLayoutChangerToFocus);
                    boolean isPrevLayout = k.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc specialFunc
                            && specialFunc.action() == KeyboardLayout.KeyFunction.SpecialFunc.Action.PREVIOUS_LAYOUT;
                    return isOldLayout || isPrevLayout;
                })
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
        for (List<KeyboardLayout.Key> row : layout.keys()) {
            float x = this.getX();
            for (KeyboardLayout.Key key : row) {
                float keyWidth = key.width() * unitWidth;

                var keyWidget = new KeyWidget(
                        (int) x, (int) y, (int) keyWidth, (int) keyHeight,
                        key, this
                );

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
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        this.keys.forEach(k -> k.onControllerInput(controller));

        // prevent default button handling for gui press which would send enter which is most likely submit
        return ControlifyBindings.GUI_PRESS.on(controller).guiPressed().get();
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

    public void setInputTarget(InputTarget inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    public InputTarget getInputTarget() {
        return inputConsumer;
    }

    public ResourceLocation getCurrentLayoutId() {
        return this.currentLayout;
    }

    public Optional<ResourceLocation> getPreviousLayoutId() {
        return Optional.ofNullable(this.previousLayout);
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
        if (focused != null) {
            if (!(focused instanceof KeyWidget)) {
                throw new IllegalArgumentException("Focused widget must be a KeyWidget in this KeyboardWidget");
            }

            // This case happens when mouse clicking on a change_layout key
            // since the action happens first which removes the key from the list,
            // and then the container sets the focus, which is no longer in the key list.
            if (!this.keys.contains(focused)) {
                focused = null;
            }
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
