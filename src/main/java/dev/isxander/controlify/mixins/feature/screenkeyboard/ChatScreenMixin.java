package dev.isxander.controlify.mixins.feature.screenkeyboard;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardDucky;
import dev.isxander.controlify.screenkeyboard.ChatKeyboardWidget;
import dev.isxander.controlify.screenkeyboard.KeyPressConsumer;
import dev.isxander.controlify.screenkeyboard.KeyboardWidget;
import net.minecraft.client.gui.components.EditBox;
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

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements ChatKeyboardDucky {
    @Unique
    private boolean keyboardExists;

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
            if (!c.genericConfig().config().showOnScreenKeyboard) return;

            this.keyboardExists = true;
            int keyboardHeight = this.height / 2;
            this.addRenderableWidget(new ChatKeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, KeyPressConsumer.of(
                    (keycode, scancode, modifiers) -> {
                        input.keyPressed(keycode, scancode, modifiers);
                        this.keyPressed(keycode, scancode, modifiers);
                    },
                    (codePoint, modifiers) -> {
                        this.charTyped(codePoint, modifiers);
                        input.charTyped(codePoint, modifiers);
                    }
            )));
        });
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen$1;<init>(Lnet/minecraft/client/gui/screens/ChatScreen;Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/Component;)V"), index = 3)
    private int modifyInputBoxY(int y) {
        if (keyboardExists)
            return y - this.height / 2;
        return y;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), index = 1)
    private int modifyInputBoxBackgroundY(int y) {
        if (keyboardExists)
            return y - this.height / 2;
        return y;
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "CONSTANT", args = "intValue=10"))
    private int modifyMaxSuggestionCount(int count) {
        return keyboardExists ? 8 : count;
    }

    @Override
    public boolean controlify$hasKeyboard() {
        return keyboardExists;
    }
}
