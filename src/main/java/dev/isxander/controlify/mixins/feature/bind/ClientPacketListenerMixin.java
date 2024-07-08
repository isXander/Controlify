package dev.isxander.controlify.mixins.feature.bind;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    /**
     * Change 'Press [key]' to 'Press [controller button]' when adding dismount tip.
     */
    @ModifyExpressionValue(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component useControllerTextForSneakTip(Component original) {
        return ControlifyApi.get().getCurrentController()
                .flatMap(c -> ControlifyApi.get().currentInputMode().isController() ? Optional.of(c) : Optional.empty())
                .flatMap(c -> Optional.ofNullable(ControlifyBindings.SNEAK.on(c)))
                .map(InputBinding::inputIcon)
                .orElse(original);
    }
}
