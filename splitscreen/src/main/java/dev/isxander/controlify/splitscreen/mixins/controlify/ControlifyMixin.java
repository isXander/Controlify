package dev.isxander.controlify.splitscreen.mixins.controlify;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.splitscreen.ControlifyExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Controlify.class)
public class ControlifyMixin {
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "sendToast", method = "Ldev/isxander/controlify/utils/ToastUtils;sendToast(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V")
    @Expression("sendToast(?, @(translatable('controlify.toast.controller_connected.description', ?)), ?)")
    @ModifyExpressionValue(method = "onControllerAdded", at = @At("MIXINEXTRAS:EXPRESSION"))
    private MutableComponent modifyHotplugControllerDescription(MutableComponent component, @Local(argsOnly = true) ControllerEntity controller) {
        int controllersConnected = Controlify.instance()
                .getControllerManager().orElseThrow()
                .getConnectedControllers().size();
        if (controllersConnected > 1) {
            InputBinding binding = ControlifyExtension.ADD_PLAYER_BIND.on(controller);
            return Component.literal("Press ").append(binding.inputIcon()).append(" to start splitscreen");
        }
        return component;
    }
}
