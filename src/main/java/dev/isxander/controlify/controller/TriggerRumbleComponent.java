package dev.isxander.controlify.controller;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TriggerRumbleComponent implements ECSComponent {
    public static final ResourceLocation ID = Controlify.id("trigger_rumble");

    private TriggerRumbleState state = null;

    public void queueTriggerRumble(@NotNull TriggerRumbleState state) {
        this.state = state;
    }

    public Optional<TriggerRumbleState> consumeTriggerRumble() {
        TriggerRumbleState state = this.state;
        this.state = null;
        return Optional.ofNullable(state);
    }
}
