package dev.isxander.controlify.screenkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class KeyboardWidget<T extends KeyboardWidget.Key> extends AbstractWidget implements ContainerEventHandler {
    protected final List<T> keys;
    protected final KeyPressConsumer keyPressConsumer;

    protected boolean shiftMode;

    private @Nullable GuiEventListener focused;
    private boolean isDragging;

    public KeyboardWidget(int x, int y, int width, int height, KeyPressConsumer keyPressConsumer) {
        super(x, y, width, height, Component.literal("On-screen keyboard"));
        this.keyPressConsumer = keyPressConsumer;
        this.keys = new ArrayList<>();
        arrangeKeys();
    }

    protected abstract void arrangeKeys();

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), 0x80000000);
        guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), 0xFFAAAAAA);

        for (T key : keys) {
            key.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public static class Key extends AbstractWidget implements ComponentProcessor {
        private static final ResourceLocation TEXTURE = Controlify.id("keyboard/key");

        private final KeyboardWidget<?> keyboard;

        private final KeyFunction normalFunction;
        private final KeyFunction shiftedFunction;

        private boolean highlighted;

        public Key(int x, int y, int width, int height, KeyFunction normalFunction, @Nullable KeyFunction shiftedFunction, KeyboardWidget<?> keyboard) {
            super(x, y, width, height, Component.literal("Key"));
            this.keyboard = keyboard;
            this.normalFunction = normalFunction;
            if (shiftedFunction != null)
                this.shiftedFunction = shiftedFunction;
            else
                this.shiftedFunction = normalFunction;
        }

        public Key(int x, int y, int width, int height, Pair<KeyFunction, KeyFunction> functions, KeyboardWidget<?> keyboard) {
            this(x, y, width, height, functions.getFirst(), functions.getSecond(), keyboard);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(TEXTURE, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);

            if (keyboard.shiftMode) {
                shiftedFunction.renderer.render(guiGraphics, mouseX, mouseY, partialTick, this);
            } else {
                normalFunction.renderer.render(guiGraphics, mouseX, mouseY, partialTick, this);
            }

            if (isHoveredOrFocused()) {
                guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), -1);
            }
        }

        @Override
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
            if (controller.bindings().GUI_PRESS.justPressed()) {
                onPress();
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY)) {
                onPress();
                return true;
            }
            return false;
        }

        protected void onPress() {
            if (keyboard.shiftMode) {
                shiftedFunction.consumer.accept(keyboard.keyPressConsumer, this);
            } else {
                normalFunction.consumer.accept(keyboard.keyPressConsumer, this);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

        public boolean isHighlighted() {
            return highlighted;
        }

        public static KeyBuilder<Key> builder(Pair<KeyFunction, KeyFunction> functions) {
            return (x, y, w, h, kb) -> new Key(x, y, w, h, functions.getFirst(), functions.getSecond(), kb);
        }

        public static KeyBuilder<Key> builder(KeyFunction normalFunction, KeyFunction shiftedFunction) {
            return (x, y, w, h, kb) -> new Key(x, y, w, h, normalFunction, shiftedFunction, kb);
        }

        public interface ForegroundRenderer {
            void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Key key);

            static ForegroundRenderer text(Component text) {
                return (guiGraphics, mouseX, mouseY, partialTick, key) -> {
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, key.getX() + key.getWidth() / 2, key.getY() + key.getHeight() / 2 - 4, 0xFFFFFFFF);
                };
            }
        }
    }

    public record KeyFunction(BiConsumer<KeyPressConsumer, Key> consumer, Key.ForegroundRenderer renderer) {
        public static Pair<KeyFunction, KeyFunction> ofChar(int normalKeyCode, char normalChar, int normalModifier, int shiftedKeyCode, char shiftedChar, int shiftedModifier) {
            return Pair.of(
                    new KeyFunction((screen, key) -> {
                        screen.acceptKeyCode(normalKeyCode, 0, normalModifier);
                        screen.acceptChar(normalChar, normalModifier);
                    }, Key.ForegroundRenderer.text(Component.literal(String.valueOf(normalChar)))),
                    new KeyFunction((screen, key) -> {
                        screen.acceptKeyCode(shiftedKeyCode, 0, shiftedModifier);
                        screen.acceptChar(shiftedChar, shiftedModifier);
                    }, Key.ForegroundRenderer.text(Component.literal(String.valueOf(shiftedChar)))
                    )
            );
        }

        public static Pair<KeyFunction, KeyFunction> ofChar(int keyCode, char ch) {
            return ofChar(keyCode, Character.toLowerCase(ch), 0, keyCode, Character.toUpperCase(ch), GLFW.GLFW_MOD_SHIFT);
        }

        public static Pair<KeyFunction, KeyFunction> ofRegularKey(int keycode, String normal) {
            KeyFunction function = new KeyFunction((screen, key) -> screen.acceptKeyCode(keycode, 0, 0), Key.ForegroundRenderer.text(Component.literal(normal)));
            return Pair.of(function, function);
        }

        public static Pair<KeyFunction, KeyFunction> ofShiftableKey(int normalKeyCode, int normalModifier, String normalName, int shiftKeyCode, int shiftModifier, String shiftName) {
            return Pair.of(
                    new KeyFunction((screen, key) -> screen.acceptKeyCode(normalKeyCode, 0, normalModifier), Key.ForegroundRenderer.text(Component.literal(normalName))),
                    new KeyFunction((screen, key) -> screen.acceptKeyCode(shiftKeyCode, 0, shiftModifier), Key.ForegroundRenderer.text(Component.literal(shiftName)))
            );
        }

        public static Pair<KeyFunction, KeyFunction> ofShiftableKey(int keyCode, String normal, String shift) {
            return ofShiftableKey(keyCode, 0, normal, keyCode, GLFW.GLFW_MOD_SHIFT, shift);
        }

        public Pair<KeyFunction, KeyFunction> copyShifted() {
            return Pair.of(this, this);
        }
    }

    public static class KeyLayoutBuilder<T extends Key> {
        private final List<List<KeyLayout<T>>> layout;
        private final float maxUnitWidth;
        private final int rowCount;

        private final KeyboardWidget<T> keyboard;

        private float currentWidth;
        private int currentRow;

        public KeyLayoutBuilder(float maxUnitWidth, int rowCount, KeyboardWidget<T> keyboard) {
            this.maxUnitWidth = maxUnitWidth;
            this.rowCount = rowCount;
            this.keyboard = keyboard;
            this.layout = new ArrayList<>(rowCount);
            for (int i = 0; i < rowCount; i++) {
                layout.add(new ArrayList<>());
            }
        }

        public void key(KeyBuilder<T> key, float width) {
            Validate.isTrue(currentWidth + width <= maxUnitWidth, "Key width exceeds row width");

            layout.get(currentRow).add(new KeyLayout<>(key, width));

            currentWidth += width;
        }

        public void nextRow() {
            Validate.isTrue(currentRow < rowCount, "Row index out of bounds");

            currentWidth = 0;
            currentRow++;
        }

        public void build(Consumer<T> keyConsumer) {
            int trueWidth = keyboard.getWidth();
            int trueHeight = keyboard.getHeight();

            float unitWidth = (float) trueWidth / maxUnitWidth;
            float keyHeight = (float) trueHeight / rowCount;

            float y = keyboard.getY();
            for (List<KeyLayout<T>> row : layout) {
                float x = keyboard.getX();
                for (KeyLayout<T> keyLayout : row) {
                    float keyWidth = unitWidth * keyLayout.unitWidth;
                    T key = keyLayout.keyBuilder.build((int) x, (int) y, (int) keyWidth, (int) keyHeight, keyboard);
                    keyConsumer.accept(key);

                    x += keyWidth;
                }

                y += keyHeight;
            }
        }

        private record KeyLayout<T extends Key>(KeyBuilder<T> keyBuilder, float unitWidth) {}
    }

    @FunctionalInterface
    public interface KeyBuilder<T extends Key> {
        T build(int x, int y, int width, int height, KeyboardWidget<T> keyboard);
    }

    @Override
    public @NotNull List<T> children() {
        return keys;
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
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
