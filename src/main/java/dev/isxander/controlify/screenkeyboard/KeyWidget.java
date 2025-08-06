package dev.isxander.controlify.screenkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.Controlify;
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
    public static final ResourceLocation SPRITE_PRESSED = CUtil.rl("keyboard/key_pressed");

    private final KeyboardWidget keyboard;
    private final KeyboardLayout.ShiftableKey key;

    private final Component unshiftedLabel, shiftedLabel;

    private boolean shortcutPressed;
    private final HoldRepeatHelper holdRepeatHelper;

    private boolean buttonPressed, mousePressed;

    public KeyWidget(int x, int y, int width, int height, KeyboardLayout.ShiftableKey key, KeyboardWidget keyboard) {
        super(x, y, width, height, Component.literal("Key"));
        this.keyboard = keyboard;
        this.key = key;
        this.holdRepeatHelper = new HoldRepeatHelper(10, 2);

        this.unshiftedLabel = createLabel(key, false);
        this.shiftedLabel = createLabel(key, true);
    }

    public void renderKeyBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.isFocused()) {
            // the call in overrideControllerButtons won't be triggered to un-press if the key is not focused
            this.buttonPressed = false;
        }

        Blit.sprite(graphics, isVisuallyPressed() ? SPRITE_PRESSED : SPRITE, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);

        if (isHoveredOrFocused()) {
            graphics.renderOutline(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0x80FFFFFF);
        } else if (!shortcutPressed) {
            this.holdRepeatHelper.reset();
        }
    }

    public void renderKeyForeground(GuiGraphics graphics, int mouseX, int mouseY, float deltaTick) {
        Component label = this.keyboard.isShifted() ? this.shiftedLabel : this.unshiftedLabel;
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                label,
                getX() + getWidth() / 2,
                getY() + getHeight() / 2 - 4 + (isVisuallyPressed() ? 2 : 0),
                0xFFFFFFFF
        );
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // custom rendered above
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        var guiPress = ControlifyBindings.GUI_PRESS.on(controller);

        // this means if you were holding the button down and navigated to this key,
        // it would not show as pressed until you release and press again
        if (guiPress.justPressed()) {
            this.buttonPressed = true;
        } else if (guiPress.justReleased()) {
            this.buttonPressed = false;
        }

        // prevent the press action if the button was navigated to whilst holding the button down
        // the above visual state will not be pressed, so this should not trigger either
        if (this.buttonPressed && holdRepeatHelper.shouldAction(guiPress)) {
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
            this.mousePressed = true;
            onPress();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mousePressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void onPress() {
        KeyboardLayout.Key key = this.getKey();
        KeyboardInputConsumer inputConsumer = this.keyboard.getInputConsumer();

        ScreenProcessor.playClackSound();

        boolean wasShiftAction = false;

        switch (key) {
            case KeyboardLayout.Key.StringKey stringKey ->
                insertText(stringKey.string(), inputConsumer);

            case KeyboardLayout.Key.CodeKey codeKey ->
                    inputConsumer.acceptKeyCode(codeKey.keycode(), codeKey.scancode(), codeKey.modifier());

            case KeyboardLayout.Key.SpecialKey specialKey -> {
                switch (specialKey.action()) {
                    case SHIFT -> {
                        if (!this.keyboard.isShiftLocked()) {
                            this.keyboard.setShifted(!this.keyboard.isShifted());
                        }
                        wasShiftAction = true;
                    }
                    case SHIFT_LOCK -> {
                        boolean shiftLocked = !this.keyboard.isShiftLocked();
                        this.keyboard.setShiftLocked(shiftLocked);
                        this.keyboard.setShifted(shiftLocked);

                        wasShiftAction = true;
                    }

                    case ENTER -> inputConsumer.acceptKeyCode(InputConstants.KEY_RETURN, 0, 0);
                    case BACKSPACE -> inputConsumer.acceptKeyCode(InputConstants.KEY_BACKSPACE, 0, 0);
                    case TAB -> inputConsumer.acceptKeyCode(InputConstants.KEY_TAB, 0, 0);
                    case LEFT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_LEFT, 0, 0);
                    case RIGHT_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_RIGHT, 0, 0);
                    case UP_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_UP, 0, 0);
                    case DOWN_ARROW -> inputConsumer.acceptKeyCode(InputConstants.KEY_DOWN, 0, 0);

                    case PASTE -> {
                        String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
                        insertText(clipboard, inputConsumer);
                    }

                    default -> {
                        CUtil.LOGGER.warn("Unhandled code action: " + specialKey.action());
                    }
                }
            }

            case KeyboardLayout.Key.ChangeLayoutKey changeLayoutKey -> {
                ResourceLocation layoutId = changeLayoutKey.otherLayout();
                KeyboardLayoutWithId layoutWithId = Controlify.instance().keyboardLayoutManager().getLayout(layoutId);
                this.keyboard.updateLayout(layoutWithId);
            }
        }

        if (!wasShiftAction && this.keyboard.isShifted() && !this.keyboard.isShiftLocked()) {
            // the key is shiftable if the key identity is different (i.e. it's a different key when shifted)
            if (this.key.regular() != this.key.shifted()) {
                this.keyboard.setShifted(false);
            }
        }
    }

    public KeyboardLayout.Key getKey() {
        return this.key.get(this.keyboard.isShifted());
    }

    public boolean isVisuallyPressed() {
        return this.buttonPressed || this.shortcutPressed || this.mousePressed;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private static void insertText(String text, KeyboardInputConsumer inputConsumer) {
        text.codePoints().forEach((codePoint) -> {
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
