/*? if simple-voice-chat {*/
package dev.isxander.controlify.compatibility.simplevoicechat.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.maxhenkel.voicechat.voice.client.PTTKeyHandler;
import dev.isxander.controlify.compatibility.simplevoicechat.SimpleVoiceChatCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PTTKeyHandler.class, remap = false)
public class PTTKeyHandlerMixin {
    @ModifyExpressionValue(
            method = {
                    "isPTTDown()Z",
                    "isAnyDown()Z",
            },
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/PTTKeyHandler;pttKeyDown:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean isControllerPTTDown(boolean keyDown) {
        return keyDown || SimpleVoiceChatCompat.isPTTDown();
    }

    @ModifyExpressionValue(
            method = {
                    "isWhisperDown()Z",
                    "isAnyDown()Z",
            },
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/PTTKeyHandler;whisperKeyDown:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean isControllerWhisperDown(boolean keyDown) {
        return keyDown || SimpleVoiceChatCompat.isWhisperDown();
    }
}
/*?}*/
