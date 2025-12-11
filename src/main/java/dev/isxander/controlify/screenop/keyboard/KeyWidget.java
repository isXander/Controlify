package dev.isxander.controlify.screenop.keyboard;

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
import dev.isxander.controlify.utils.render.CGuiPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Represents a single key widget within a {@link KeyboardWidget}.
 * It handles rendering, input processing, and interaction with the keyboard layout.
 */
public class KeyWidget extends AbstractWidget implements ComponentProcessor, ScreenControllerEventListener {
    public static final Identifier SPRITE = CUtil.rl("keyboard/key");
    public static final Identifier SPRITE_PRESSED = CUtil.rl("keyboard/key_pressed");

    private final KeyboardWidget keyboard;
    private final KeyboardLayout.Key key;

    private final Component regularLabel, shiftedLabel;
    private final boolean supportsRegular, supportsShifted;

    private boolean shortcutPressed;
    private final HoldRepeatHelper holdRepeatHelper;

    private boolean buttonPressed, mousePressed;

    private final int renderScale;
    private final int renderWidth, renderHeight;

    public KeyWidget(int x, int y, int width, int height, int renderScale, KeyboardLayout.Key key, KeyboardWidget keyboard) {
        super(x, y, width, height, Component.literal("Key"));
        this.keyboard = keyboard;
        this.key = key;
        this.holdRepeatHelper = new HoldRepeatHelper(10, 2);

        this.regularLabel = createLabel(key, false);
        this.shiftedLabel = createLabel(key, true);
        this.supportsRegular = supportsAction(false);
        this.supportsShifted = supportsAction(true);

        this.renderScale = renderScale;
        this.renderWidth = width / renderScale;
        this.renderHeight = height / renderScale;
    }

    public void renderKeyBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.active = this.supportsAction();

        if (!this.isFocused()) {
            // the call in overrideControllerButtons won't be triggered to un-press if the key is not focused
            this.buttonPressed = false;
        }

        doScaledRender(graphics, () -> {
            Blit.sprite(graphics, isVisuallyPressed() ? SPRITE_PRESSED : SPRITE, getX() + 1, getY() + 1, renderWidth - 2, renderHeight - 2);

            if (isHoveredOrFocused()) {
                graphics./*? if >=1.21.9 && <1.21.11 {*//*submitOutline*//*?} else {*/renderOutline/*?}*/(getX() - 1, getY() - 1, renderWidth + 2, renderHeight + 2, 0x80FFFFFF);
            } else if (!shortcutPressed) {
                this.holdRepeatHelper.reset();
            }

            if (!this.active) {
                // gray out the key if it does not support the action
                graphics.fill(getX() + 1, getY() + 1, getX() + renderWidth - 1, getY() + renderHeight - 1, 0x30000000);
            }
        });
    }

    public void renderKeyForeground(GuiGraphics graphics, int mouseX, int mouseY, float deltaTick) {
        doScaledRender(graphics, () -> {
            Component label = this.keyboard.isShifted() ? this.shiftedLabel : this.regularLabel;
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    label,
                    getX() + renderWidth / 2,
                    getY() + renderHeight / 2 - 4 + (isVisuallyPressed() ? 2 : 0),
                    0xFFFFFFFF
            );
        });
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

        return false;
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
    //? if >=1.21.9 {
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
    *///?}
        if (isMouseOver(mouseX, mouseY)) {
            this.mousePressed = true;
            onPress();
            return true;
        }
        return false;
    }

    //? if >=1.21.9 {
    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent) {
        this.mousePressed = false;
        return super.mouseReleased(mouseButtonEvent);
    }
    //?} else {
    /*@Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mousePressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *///?}

    private void onPress() {
        KeyboardLayout.KeyFunction keyFunction = this.getKeyFunction();
        InputTarget inputConsumer = this.keyboard.getInputTarget();

        ScreenProcessor.playClackSound();

        boolean wasShiftAction = false;

        switch (keyFunction) {
            case KeyboardLayout.KeyFunction.StringFunc stringKey ->
                insertText(stringKey.string(), inputConsumer);

            case KeyboardLayout.KeyFunction.CodeFunc codeKey ->
                codeKey.codes().forEach(code -> inputConsumer.acceptKeyCode(code.keycode(), code.scancode(), code.modifier()));

            case KeyboardLayout.KeyFunction.SpecialFunc specialKey -> {
                switch (specialKey.action()) {
                    case SHIFT -> {
                        toggleShift();
                        wasShiftAction = true;
                    }
                    case SHIFT_LOCK -> {
                        toggleShiftLock();
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
                    case COPY_ALL -> {
                        inputConsumer.copy();
                    }

                    case PREVIOUS_LAYOUT -> {
                        this.keyboard.getPreviousLayoutId().ifPresent(this::changeLayout);
                    }

                    default -> {
                        CUtil.LOGGER.warn("Unhandled code action: " + specialKey.action());
                    }
                }
            }

            case KeyboardLayout.KeyFunction.ChangeLayoutFunc func ->
                this.changeLayout(func.layout());
        }

        if (!wasShiftAction && this.keyboard.isShifted() && !this.keyboard.isShiftLocked()) {
            // the key is shiftable if the key identity is different (i.e. it's a different key when shifted)
            if (this.key.regular() != this.key.shifted()) {
                this.keyboard.setShifted(false);
            }
        }
    }

    protected void toggleShift() {
        if (!this.keyboard.isShiftLocked()) {
            this.keyboard.setShifted(!this.keyboard.isShifted());
        } else {
            this.keyboard.setShifted(false);
            this.keyboard.setShiftLocked(false);
        }
    }

    protected void toggleShiftLock() {
        boolean shiftLocked = !this.keyboard.isShiftLocked();
        this.keyboard.setShiftLocked(shiftLocked);
        this.keyboard.setShifted(shiftLocked);
    }

    public KeyboardLayout.Key getKey() {
        return this.key;
    }

    public KeyboardLayout.KeyFunction getKeyFunction() {
        return this.key.getFunction(this.keyboard.isShifted());
    }

    public boolean isVisuallyPressed() {
        return this.buttonPressed
                || this.shortcutPressed
                || this.mousePressed
                || this.isShiftKeyAndShifting()
                || this.isShiftLockKeyAndShiftLocked();
    }

    private boolean isShiftKeyAndShifting() {
        return this.keyboard.isShifted() && !this.keyboard.isShiftLocked()
                && this.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc(KeyboardLayout.KeyFunction.SpecialFunc.Action action)
                && action == KeyboardLayout.KeyFunction.SpecialFunc.Action.SHIFT;
    }

    private boolean isShiftLockKeyAndShiftLocked() {
        return this.keyboard.isShiftLocked()
                && this.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc(KeyboardLayout.KeyFunction.SpecialFunc.Action action)
                && action == KeyboardLayout.KeyFunction.SpecialFunc.Action.SHIFT_LOCK;
    }

    private boolean supportsAction(boolean shifted) {
        boolean supportsCharInput = this.keyboard.getInputTarget().supportsCharInput();
        boolean supportsKeyCodeInput = this.keyboard.getInputTarget().supportsKeyCodeInput();
        boolean supportsCopying = this.keyboard.getInputTarget().supportsCopying();

        return switch (this.getKey().getFunction(shifted)) {
            case KeyboardLayout.KeyFunction.StringFunc ignored -> supportsCharInput;
            case KeyboardLayout.KeyFunction.CodeFunc ignored -> supportsKeyCodeInput;
            case KeyboardLayout.KeyFunction.SpecialFunc specialFunc ->
                switch (specialFunc.action()) {
                    case ENTER, BACKSPACE, LEFT_ARROW, RIGHT_ARROW, UP_ARROW, DOWN_ARROW -> supportsKeyCodeInput;
                    case TAB, PASTE -> supportsCharInput;
                    case COPY_ALL -> supportsCopying;
                    case SHIFT, SHIFT_LOCK, PREVIOUS_LAYOUT -> true;
                };
            case KeyboardLayout.KeyFunction.ChangeLayoutFunc ignored -> true;
        };
    }

    private boolean supportsAction() {
        return this.keyboard.isShifted() ? this.supportsShifted : this.supportsRegular;
    }

    private void changeLayout(Identifier layoutId) {
        KeyboardLayoutWithId layoutWithId = Controlify.instance().keyboardLayoutManager().getLayout(layoutId);
        this.keyboard.updateLayout(layoutWithId);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private void doScaledRender(GuiGraphics graphics, Runnable runnable) {
        var pose = CGuiPose.ofPush(graphics);
        pose.translate(getX(), getY());
        pose.scale(this.renderScale, this.renderScale);
        pose.translate(-getX(), -getY());

        runnable.run();

        pose.pop();
    }

    private static void insertText(String text, InputTarget inputConsumer) {
        // One `char` is not necessarily one visible character.
        // Some characters, such as emojis, are represented using surrogate pairs,
        // meaning they span two `char`s.
        // Code points are used to represent characters that *may* be represented by surrogate pairs.
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

    private static Component createLabel(KeyboardLayout.Key key, boolean shift) {
        KeyboardLayout.KeyFunction keyFunction = key.getFunction(shift);

        return key.shortcutBinding()
                .map(b -> BindingFontHelper.binding(b.bindId()))
                .<Component>map(glyph -> Component.empty()
                        .append(glyph)
                        .append(" ")
                        .append(keyFunction.displayName()))
                .orElseGet(keyFunction::displayName);
    }
}
