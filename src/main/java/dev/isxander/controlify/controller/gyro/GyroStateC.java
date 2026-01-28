package dev.isxander.controlify.controller.gyro;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3fc;

/**
 * Represents the current gyro delta in radians per second.
 */
public interface GyroStateC extends Vector3fc {
    GyroStateC ZERO = new GyroState(0, 0, 0);

    default float pitch() {
        return x();
    }

    default float yaw() {
        return y();
    }

    default float roll() {
        return z();
    }

    Codec<GyroStateC> CODEC_MUTABLE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("pitch").forGetter(GyroStateC::pitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(GyroStateC::yaw),
            Codec.FLOAT.fieldOf("roll").forGetter(GyroStateC::roll)
    ).apply(instance, GyroState::new));
}
