package dev.isxander.splitscreen.client.mixins.controlify;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.splitscreen.client.integrations.ControlifyExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Controlify.class)
public class ControlifyMixin {
    /**
     * Modify the toast message when a controller is connected to say
     * "Press <input> to start splitscreen" if there are multiple controllers connected.
     * @param component original toast content
     * @param controller the controller that was added
     * @return the modified toast content
     */
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
            return Component.translatable("controlify.splitscreen.toast.long_press_to_join", binding.inputIcon());
        }
        return component;
    }
}
