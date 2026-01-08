package dev.isxander.controlify.controller.rumble;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class TriggerRumbleComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("trigger_rumble");

    private TriggerRumbleState state = null;

    public void queueTriggerRumble(@NonNull TriggerRumbleState state) {
        this.state = state;
    }

    public Optional<TriggerRumbleState> consumeTriggerRumble() {
        TriggerRumbleState state = this.state;
        this.state = null;
        return Optional.ofNullable(state);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
