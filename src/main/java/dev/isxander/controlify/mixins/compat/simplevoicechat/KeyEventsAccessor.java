package dev.isxander.controlify.mixins.compat.simplevoicechat;

import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(value = KeyEvents.class, remap = false)
public interface KeyEventsAccessor {
    @Invoker
    boolean invokeCheckConnected();
}
