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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class KeyboardWidget extends AbstractWidget implements ContainerEventHandler {
    private boolean shiftMode;
    private final KeyPressConsumer keyPressConsumer;

    private final List<Key> keys;
    private @Nullable GuiEventListener focused;
    private boolean isDragging;
    private final Minecraft minecraft;

    public KeyboardWidget(int x, int y, int width, int height, KeyPressConsumer keyPressConsumer) {
        super(x, y, width, height, Component.literal("On-screen keyboard"));
        this.keyPressConsumer = keyPressConsumer;
        this.keys = new ArrayList<>();
        this.minecraft = Minecraft.getInstance();
        arrangeKeys();
    }

    protected void arrangeKeys() {
        KeyLayoutBuilder builder = new KeyLayoutBuilder(14, 5);

        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_ESCAPE, "Esc"), 2f);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_1, '1'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_2, '2'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_3, '3'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_4, '4'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_5, '5'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_6, '6'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_7, '7'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_8, '8'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_9, '9'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_0, '0'), 1);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_BACKSPACE, "Backspace"), 2f);

        builder.nextRow();

        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_TAB, "Tab"), 2f);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_Q, 'q'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_W, 'w'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_E, 'e'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_R, 'r'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_T, 't'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_Y, 'y'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_U, 'u'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_I, 'i'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_O, 'o'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_P, 'p'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_BACKSLASH, '\\'), 2f);

        builder.nextRow();

        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_CAPSLOCK, "Caps"), 2f);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_A, 'a'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_S, 's'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_D, 'd'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_F, 'f'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_G, 'g'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_H, 'h'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_J, 'j'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_K, 'k'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_L, 'l'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_APOSTROPHE, '\'', 0, InputConstants.KEY_APOSTROPHE, '"', GLFW.GLFW_MOD_SHIFT), 1);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_RETURN, "Enter"), 2f);

        builder.nextRow();

        builder.key(new KeyFunction((screen, key) -> {
            shiftMode = !shiftMode;
            key.setHighlighted(shiftMode);
        }, Key.ForegroundRenderer.text(Component.literal("Shift"))).copyShifted(), 2f);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_Z, 'z'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_X, 'x'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_C,'c'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_V, 'v'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_B, 'b'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_N, 'n'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_M, 'm'), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_COMMA, ',', 0, InputConstants.KEY_PERIOD, '.', 0), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_1, '!', GLFW.GLFW_MOD_SHIFT, InputConstants.KEY_SLASH, '?', GLFW.GLFW_MOD_SHIFT), 1);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_SLASH, '/', 0, InputConstants.KEY_BACKSLASH, '\\', 0), 1);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_UP, "↑"), 1f);

        builder.nextRow();

        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_LCONTROL, "Ctrl"), 2f);
        builder.key(KeyFunction.ofChar(InputConstants.KEY_SPACE, ' '), 9f);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_LEFT, "←"), 1f);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_DOWN, "↓"), 1f);
        builder.key(KeyFunction.ofRegularKey(InputConstants.KEY_RIGHT, "→"), 1f);

        builder.build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), 0x80000000);
        guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), 0xFFAAAAAA);

        for (Key key : keys) {
            key.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public class Key extends AbstractWidget implements ComponentProcessor {
        private static final ResourceLocation TEXTURE = Controlify.id("keyboard/key");

        private final KeyFunction normalFunction;
        private final KeyFunction shiftedFunction;

        private boolean highlighted;

        public Key(int x, int y, int width, int height, KeyFunction normalFunction, @Nullable KeyFunction shiftedFunction) {
            super(x, y, width, height, Component.literal("Key"));
            this.normalFunction = normalFunction;
            if (shiftedFunction != null)
                this.shiftedFunction = shiftedFunction;
            else
                this.shiftedFunction = normalFunction;
        }

        public Key(int x, int y, int width, int height, Pair<KeyFunction, KeyFunction> functions) {
            this(x, y, width, height, functions.getFirst(), functions.getSecond());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(TEXTURE, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);

            if (shiftMode) {
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
            if (shiftMode) {
                shiftedFunction.consumer.accept(keyPressConsumer, this);
            } else {
                normalFunction.consumer.accept(keyPressConsumer, this);
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

    public class KeyLayoutBuilder {
        private final List<List<KeyLayout>> layout;
        private final int columnCount, rowCount;

        private int currentRow;

        public KeyLayoutBuilder(int columnCount, int rowCount) {
            this.columnCount = columnCount;
            this.rowCount = rowCount;
            this.layout = new ArrayList<>(rowCount);
            for (int i = 0; i < rowCount; i++) {
                layout.add(new ArrayList<>(columnCount));
            }
        }

        public void key(Pair<KeyFunction, KeyFunction> functions, float width) {
            key((x, y, w, h) -> new Key(x, y, w, h, functions), width);
        }

        public void key(KeyFunction normalFunction, KeyFunction shiftedFunction, float width) {
            key((x, y, w, h) -> new Key(x, y, w, h, normalFunction, shiftedFunction), width);
        }

        public void key(KeyBuilder key, float width) {
            layout.get(currentRow).add(new KeyLayout(key, width));
        }

        public void nextRow() {
            Validate.isTrue(currentRow < rowCount, "Row index out of bounds");

            currentRow++;
        }

        public void build() {
            int trueWidth = getWidth();
            int trueHeight = getHeight();

            float unitWidth = (float) trueWidth / columnCount;
            float keyHeight = (float) trueHeight / rowCount;

            float y = getY();
            for (List<KeyLayout> row : layout) {
                float x = getX();
                for (KeyLayout keyLayout : row) {
                    float keyWidth = unitWidth * keyLayout.unitWidth;
                    Key key = keyLayout.keyBuilder.build((int) x, (int) y, (int) keyWidth, (int) keyHeight);
                    keys.add(key);

                    x += keyWidth;
                }

                y += keyHeight;
            }
        }

        private record KeyLayout(KeyBuilder keyBuilder, float unitWidth) {}

        @FunctionalInterface
        public interface KeyBuilder {
            Key build(int x, int y, int width, int height);
        }
    }

    @Override
    public List<Key> children() {
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
