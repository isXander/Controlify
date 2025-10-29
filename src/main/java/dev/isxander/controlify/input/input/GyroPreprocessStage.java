package dev.isxander.controlify.input.input;

import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.UnaryEventStage;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.FloatBuffer;

public class GyroPreprocessStage implements UnaryEventStage<SensorEvent.Continuous> {
    private GyroPreprocessor preprocessor;

    public GyroPreprocessStage(GyroPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public GyroPreprocessStage(GyroPreprocessor... preprocessors) {
        this.preprocessor = GyroPreprocessor.sequence(preprocessors);
    }

    @Override
    public void onEvent(SensorEvent.Continuous event, EventSink<? super SensorEvent.Continuous> downstream) {
        if (!event.sensorType().isGyroscope()) {
            downstream.accept(event);
            return;
        }

        var gyroData = new Vector3f(event.data());
        preprocessor.preprocess(gyroData);
        float[] processedData = new float[3];
        gyroData.get(FloatBuffer.wrap(processedData));
        downstream.accept(new SensorEvent.Continuous(event.timestamp(), event.sensorType(), processedData));
    }

    public void setPreprocessor(GyroPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public void setPreprocessor(GyroPreprocessor... preprocessors) {
        this.preprocessor = GyroPreprocessor.sequence(preprocessors);
    }

    public record DeadzoneFilter(float deadzone) implements GyroPreprocessor {
        @Override
        public void preprocess(Vector3f data) {
            data.x = Math.abs(data.x) < deadzone ? 0.0f : data.x - Math.copySign(deadzone, data.x);
            data.y = Math.abs(data.y) < deadzone ? 0.0f : data.y - Math.copySign(deadzone, data.y);
            data.z = Math.abs(data.z) < deadzone ? 0.0f : data.z - Math.copySign(deadzone, data.z);
        }
    }

    public record LowPassFilter(float alpha) implements GyroPreprocessor {
        @Override
        public void preprocess(Vector3f data) {
            Vector3f previous = new Vector3f(data);
            data.mul(1.0f - alpha).add(previous.mul(alpha));
        }
    }

    public record GainFilter(float gain) implements GyroPreprocessor {
        @Override
        public void preprocess(Vector3f data) {
            data.mul(gain);
        }
    }

    public record BiasCorrection(Vector3fc bias) implements GyroPreprocessor {
        @Override
        public void preprocess(Vector3f data) {
            data.sub(bias);
        }
    }

    public interface GyroPreprocessor {
        void preprocess(Vector3f data);

        static GyroPreprocessor sequence(GyroPreprocessor... preprocessors) {
            if (preprocessors.length == 1) return preprocessors[0];
            return data -> {
                for (GyroPreprocessor preprocessor : preprocessors) {
                    preprocessor.preprocess(data);
                }
            };
        }
    }
}
