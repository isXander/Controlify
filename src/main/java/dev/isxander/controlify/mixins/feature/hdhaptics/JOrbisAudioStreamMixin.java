package dev.isxander.controlify.mixins.feature.hdhaptics;

import org.spongepowered.asm.mixin.Mixin;

/*? if >1.20.4 {*/
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.isxander.controlify.driver.sdl.DualsenseOggAudioStream;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.sounds.JOrbisAudioStream;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JOrbisAudioStream.class)
public class JOrbisAudioStreamMixin {

//                  INPUT FORMAT
//        --------------------------------
//          channel 1          channel 2
//        --------------    --------------
//        s1 s2 s3 s4 s5    s1 s2 s3 s4 s5
//
//                 OUTPUT FORMAT
//        -------------------------------------
//        sample1   sample2   sample3   sample4
//        -------   -------   -------   -------
//        ch1 ch2   ch1 ch2   ch1 ch2   ch1 ch2
    @WrapOperation(method = "readChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/JOrbisAudioStream;copyMono([FIJLit/unimi/dsi/fastutil/floats/FloatConsumer;)V"))
    private void convert1To4(float[] samples, int channelOffset, long samplesToWrite, FloatConsumer audioConsumer, Operation<Void> original) {
        if (isNotDualSenseStream()) {
            original.call(samples, channelOffset, samplesToWrite, audioConsumer);
            return;
        }

        // copy just the single channel from the sample array
        float[] samplesForChannel = new float[(int) samplesToWrite];
        System.arraycopy(samples, channelOffset, samplesForChannel, 0, (int)samplesToWrite);

        DualsenseOggAudioStream.convertMono(samplesForChannel, audioConsumer);
    }

    @WrapOperation(method = "readChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/JOrbisAudioStream;copyStereo([FI[FIJLit/unimi/dsi/fastutil/floats/FloatConsumer;)V"))
    private void convert2To4(float[] leftSamples, int leftChannelOffset, float[] rightSamples, int rightChannelOffset, long samplesToWrite, FloatConsumer audioConsumer, Operation<Void> original) {
        if (isNotDualSenseStream()) {
            original.call(leftSamples, leftChannelOffset, rightSamples, rightChannelOffset, samplesToWrite, audioConsumer);
            return;
        }

        float[] leftSamplesForChannel = new float[(int) samplesToWrite];
        System.arraycopy(leftSamples, leftChannelOffset, leftSamplesForChannel, 0, (int)samplesToWrite);

        float[] rightSamplesForChannel = new float[(int) samplesToWrite];
        System.arraycopy(rightSamples, rightChannelOffset, rightSamplesForChannel, 0, (int)samplesToWrite);

        DualsenseOggAudioStream.convertStereo(leftSamplesForChannel, rightSamplesForChannel, audioConsumer);
    }

    @WrapOperation(method = "readChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/JOrbisAudioStream;copyAnyChannels([[FI[IJLit/unimi/dsi/fastutil/floats/FloatConsumer;)V"))
    private void convertUnknownTo4(float[][] samplesByChannel, int channelCount, int[] channelOffsets, long samplesToWrite, FloatConsumer audioConsumer, Operation<Void> original) {
        if (channelCount == 4 || isNotDualSenseStream()) {
            original.call(samplesByChannel, channelCount, channelOffsets, samplesToWrite, audioConsumer);
        }

        throw new IllegalStateException("Cannot convert audio stream with " + channelCount + " channels to 4 channel stream for DualSense HD haptics");
    }

    @Unique
    private boolean isNotDualSenseStream() {
        return !(((Object) this) instanceof DualsenseOggAudioStream);
    }
}
/*?} else {*/
/*@Mixin(targets = {})
public class JOrbisAudioStreamMixin {

}
*//*?}*/

