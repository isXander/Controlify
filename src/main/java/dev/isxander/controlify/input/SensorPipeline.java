package dev.isxander.controlify.input;

import dev.isxander.controlify.input.input.GyroPreprocessStage;
import dev.isxander.controlify.input.input.SensorAccumulatorStage;
import dev.isxander.controlify.input.input.SensorEvent;
import dev.isxander.controlify.input.pipeline.Clock;
import dev.isxander.controlify.input.pipeline.EventSource;

public final class SensorPipeline {
    // continuous stages
    private final EventSource<SensorEvent.Continuous> continuousSource;
    private final GyroPreprocessStage gyroPreprocessStage;
    private final EventSource<SensorEvent.Continuous> processedContinuousSource;

    // interval stages
    private final SensorAccumulatorStage accumulatorStage;
    private final EventSource<SensorEvent.Interval> intervalSource;

    public SensorPipeline(EventSource<SensorEvent.Continuous> source, Clock clock) {
        this.continuousSource = source;

        this.processedContinuousSource = this.continuousSource
                .via(this.gyroPreprocessStage = new GyroPreprocessStage());

        this.intervalSource = this.processedContinuousSource
                .via(clock, this.accumulatorStage = new SensorAccumulatorStage());
    }

    // continuous stages
    public EventSource<SensorEvent.Continuous> sourceRawContinuous() {
        return this.continuousSource;
    }
    public GyroPreprocessStage stageGyroPreprocess() {
        return this.gyroPreprocessStage;
    }
    public EventSource<SensorEvent.Continuous> sourceProcessedContinuous() {
        return this.processedContinuousSource;
    }

    // interval stages
    public SensorAccumulatorStage stageSensorAccumulator() {
        return this.accumulatorStage;
    }
    public EventSource<SensorEvent.Interval> sourceInterval() {
        return this.intervalSource;
    }
}
