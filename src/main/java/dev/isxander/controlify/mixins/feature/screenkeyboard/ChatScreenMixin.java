package dev.isxander.controlify.mixins.feature.screenkeyboard;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.keyboard.NativeKeyboardComponent;
import dev.isxander.controlify.screenkeyboard.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements MixinInputTarget, ChatKeyboardDucky {
    @Unique
    private KeyboardWidget keyboard;
    @Unique
    private float shiftChatAmt = 0f;

    @Shadow
    protected EditBox input;

    @Shadow
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void addKeyboard(CallbackInfo ci) {
        ControlifyApi.get().getCurrentController().ifPresent(c -> {
            if (!ControlifyApi.get().currentInputMode().isController()) return;
            if (!c.genericConfig().config().showOnScreenKeyboard) return;

            Optional<NativeKeyboardComponent> nativeKeyboardOpt = c.nativeKeyboard();
            if (nativeKeyboardOpt.isPresent() && nativeKeyboardOpt.get().confObj().useNativeKeyboard) {
                NativeKeyboardComponent nativeKeyboard = nativeKeyboardOpt.get();

                this.shiftChatAmt = nativeKeyboard.getKeyboardHeight();
                nativeKeyboard.open();
            } else {
                this.shiftChatAmt = 0.5f;
                int keyboardHeight = (int) (this.height * this.shiftChatAmt);
                this.addRenderableWidget(keyboard = new KeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, KeyboardLayouts.chat(), this, (ChatScreen) (Object) this));
            }
        });
    }

    @ModifyArg(method = "setInitialFocus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"))
    private GuiEventListener modifyInitialFocus(GuiEventListener editBox) {
        return this.keyboard != null ? this.keyboard : editBox;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen$1;<init>(Lnet/minecraft/client/gui/screens/ChatScreen;Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/Component;)V"), index = 3)
    private int modifyInputBoxY(int y) {
        return (int) (y - this.height * this.shiftChatAmt);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), index = 1)
    private int modifyInputBoxBackgroundY(int y) {
        return (int) (y - this.height * this.shiftChatAmt);
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "CONSTANT", args = "intValue=10"))
    private int modifyMaxSuggestionCount(int count) {
        return shiftChatAmt > 0 ? 8 : count;
    }

    @Override
    public float controlify$keyboardShiftAmount() {
        return this.shiftChatAmt;
    }



    @Override
    public boolean controlify$supportsCharInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptChar(char ch, int modifiers) {
        this.input.charTyped(ch, modifiers);
        return true;
    }

    @Override
    public boolean controlify$supportsKeyCodeInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptKeyCode(int keycode, int scancode, int modifiers) {
        boolean bypassInput = List.of(
                InputConstants.KEY_RETURN,
                InputConstants.KEY_ESCAPE
        ).contains(keycode);

        if (bypassInput) {
            return ((ChatScreen) (Object) this).keyPressed(keycode, scancode, modifiers);
        }
        return this.input.keyPressed(keycode, scancode, modifiers);
    }

    @Override
    public boolean controlify$supportsCopying() {
        return true;
    }

    @Override
    public boolean controlify$copy() {
        minecraft.keyboardHandler.setClipboard(this.input.getValue());
        return true;
    }
}
