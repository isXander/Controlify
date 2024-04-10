package dev.isxander.controlify.mixins.compat.simplevoicechat;

import org.spongepowered.asm.mixin.Mixin;

/*? if simple-voice-chat {*//*
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(value = KeyEvents.class, remap = false)
public interface KeyEventsAccessor {
    @Invoker
    boolean invokeCheckConnected();
}
*//*?} else {*/
@Mixin(targets = {})
public interface KeyEventsAccessor {

}
/*?}*/
