package dev.isxander.controlify.input;

import dev.isxander.controlify.input.input.*;
import dev.isxander.controlify.input.pipeline.Clock;
import dev.isxander.controlify.input.pipeline.EventSource;
import dev.isxander.controlify.input.input.Signal;
import dev.isxander.controlify.input.input.SignalSynthesizerStage;

public final class InputPipeline {
    // input preprocessing
    private final EventSource<InputEvent> rawInputSource;
    private final DeadzoneStage deadzoneStage;
    private final DigitalAnalogueStage digitalAnalogueStage;
    private final EventSource<InputEvent> processedInputSource;

    // signal synthesis
    private final SignalSynthesizerStage signalSynthesizerStage;
    private final EventSource<Signal> synthesizedSignalSource;

    public InputPipeline(EventSource<InputEvent> source, Clock clock) {
        this.rawInputSource = source;

        this.processedInputSource = this.rawInputSource
                .via(this.deadzoneStage = new DeadzoneStage()) // do before so we don't apply deadzones to the DAC axes
                .via(this.digitalAnalogueStage = new DigitalAnalogueStage());

        this.synthesizedSignalSource = this.processedInputSource
                .via(clock, this.signalSynthesizerStage = new SignalSynthesizerStage());
    }

    // input preprocessing
    public EventSource<InputEvent> sourceRawInput() {
        return this.rawInputSource;
    }
    public DeadzoneStage stageDeadzone() {
        return this.deadzoneStage;
    }
    public DigitalAnalogueStage stageDigitalAnalogue() {
        return this.digitalAnalogueStage;
    }
    public EventSource<InputEvent> sourceProcessedInput() {
        return this.processedInputSource;
    }

    // signal synthesis
    public SignalSynthesizerStage stageSignalSynthesizer() {
        return this.signalSynthesizerStage;
    }
    public EventSource<Signal> sourceSynthesizedSignals() {
        return this.synthesizedSignalSource;
    }
}
