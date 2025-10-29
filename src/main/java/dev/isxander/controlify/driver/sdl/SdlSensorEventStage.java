package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.input.input.SensorEvent;
import dev.isxander.controlify.input.input.SensorType;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.EventStage;

/**
 * An event stage that converts SDL sensor events to Controlify sensor events.
 */
public class SdlSensorEventStage implements EventStage<SdlControllerEvent, SensorEvent.Continuous> {
    @Override
    public void onEvent(SdlControllerEvent event, EventSink<? super SensorEvent.Continuous> downstream) {
        if (event instanceof SdlControllerEvent.GamepadSensor sensorEvent) {
            SensorType sensorType = SdlInputConversions.mapSensorType(sensorEvent.sensor());
            if (sensorType == null) return;

            downstream.accept(new SensorEvent.Continuous(
                    sensorEvent.timestamp(),
                    sensorType,
                    sensorEvent.data()
            ));
        }
    }
}
