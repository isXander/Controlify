package dev.isxander.controlify.screenkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyWidget extends AbstractWidget implements ComponentProcessor, ScreenControllerEventListener {
    public static final ResourceLocation SPRITE = CUtil.rl("keyboard/key");

    private final KeyboardWidget keyboard;
    private final KeyboardLayout.ShiftableKey key;

    private final Component unshiftedLabel, shiftedLabel;

    private boolean shortcutPressed;
    private final HoldRepeatHelper holdRepeatHelper;

    public KeyWidget(int x, int y, int width, int height, KeyboardLayout.ShiftableKey key, KeyboardWidget keyboard) {
        super(x, y, width, height, Component.literal("Key"));
        this.keyboard = keyboard;
        this.key = key;
        this.holdRepeatHelper = new HoldRepeatHelper(10, 2);

        this.unshiftedLabel = createLabel(key, false);
        this.shiftedLabel = createLabel(key, true);
    }

    public void renderKeyBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Blit.sprite(graphics, SPRITE, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);

        if (isHoveredOrFocused() || this.shortcutPressed) {
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), -1);
        } else {
            this.holdRepeatHelper.reset();
        }
    }

    public void renderKeyForeground(GuiGraphics graphics, int mouseX, int mouseY, float deltaTick) {
        Component label = this.keyboard.shifting ? this.shiftedLabel : this.unshiftedLabel;
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                label,
                getX() + getWidth() / 2,
                getY() + getHeight() / 2 - 4,
                0xFFFFFFFF
        );
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // custom rendered above
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (holdRepeatHelper.shouldAction(ControlifyBindings.GUI_PRESS.on(controller))) {
            onPress();
            holdRepeatHelper.onNavigate();
        }

        return true;
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        this.key.shortcutBinding().ifPresent(supplier -> {
            InputBinding shortcutBinding = supplier.on(controller);

            this.shortcutPressed = shortcutBinding.digitalNow();

            if (this.holdRepeatHelper.shouldAction(shortcutBinding)) {
                onPress();
                this.holdRepeatHelper.onNavigate();
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button /*? if >=1.21.9 {*/, boolean doubleClick /*?}*/) {
        if (isMouseOver(mouseX, mouseY)) {
            onPress();
            return true;
        }
        return false;
    }

    private void onPress() {
        KeyboardLayout.Key key = this.getKey();
        KeyboardInputConsumer inputConsumer = this.keyboard.inputConsumer;

        switch (key) {
            case KeyboardLayout.Key.StringKey stringKey -> {
                stringKey.string().codePoints().forEach((codePoint) -> {
                    // guess the modifier based on the nature of the character
                    int modCapital = Character.isUpperCase(codePoint) ? GLFW.GLFW_MOD_SHIFT : 0;
                    int modifiers = modCapital;

                    if (Character.isBmpCodePoint(codePoint)) {
                        inputConsumer.acceptChar((char) codePoint, modifiers);
                    } else if (Character.isValidCodePoint(codePoint)) {
                        inputConsumer.acceptChar(Character.highSurrogate(codePoint), modifiers);
                        inputConsumer.acceptChar(Character.lowSurrogate(codePoint), modifiers);
                    }
                });
            }

            case KeyboardLayout.Key.CodeKey codeKey ->
                    inputConsumer.acceptKeyCode(codeKey.keycode(), codeKey.scancode(), codeKey.modifier());

            case KeyboardLayout.Key.SpecialKey specialKey -> {
                switch (specialKey.action()) {
                    case SHIFT -> this.keyboard.shifting = !this.keyboard.shifting;

                    case ENTER -> inputConsumer.acceptKeyCode(InputConstants.KEY_RETURN, 0, 0);
                    case BACKSPACE -> inputConsumer.acceptKeyCode(InputConstants.KEY_BACKSPACE, 0, 0);
                    case TAB -> inputConsumer.acceptKeyCode(InputConstants.KEY_TAB, 0, 0);
                    case LEFT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_LEFT, 0, 0);
                    case RIGHT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_RIGHT, 0, 0);
                    case UP_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_UP, 0, 0);
                    case DOWN_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_DOWN, 0, 0);
                }
            }
        }
    }

    private KeyboardLayout.Key getKey() {
        return this.key.get(this.keyboard.shifting);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private static Component createLabel(KeyboardLayout.ShiftableKey shiftableKey, boolean shift) {
        KeyboardLayout.Key key = shiftableKey.get(shift);

        return shiftableKey.shortcutBinding()
                .map(b -> BindingFontHelper.binding(b.bindId()))
                .<Component>map(glyph -> Component.empty()
                        .append(glyph)
                        .append(" ")
                        .append(key.displayName()))
                .orElseGet(key::displayName);
    }
}
