/*? if simple-voice-chat {*/
package dev.isxander.controlify.compatibility.simplevoicechat.mixins;

import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = KeyEvents.class, remap = false)
public interface KeyEventsAccessor {
    @Invoker
    boolean invokeCheckConnected();
}
/*?}*/
