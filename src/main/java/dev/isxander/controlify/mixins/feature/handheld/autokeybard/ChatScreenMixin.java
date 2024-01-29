package dev.isxander.controlify.mixins.feature.handheld.autokeybard;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.driver.global.GlobalDriver;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends ScreenMixin {
    @Override
    protected void openOnScreenKeyboard(CallbackInfo ci) {
        Controlify.instance().getCurrentController().ifPresent(controller -> {
            if (controller.config().showOnScreenKeyboard) {
                GlobalDriver.get().onScreenKeyboard().openOnScreenKeyboard(0, 0, 0, 0);
            }
        });
    }

    @Override
    protected void closeOnScreenKeyboard(CallbackInfo ci) {
        GlobalDriver.get().onScreenKeyboard().closeOnScreenKeyboard();
    }
}
