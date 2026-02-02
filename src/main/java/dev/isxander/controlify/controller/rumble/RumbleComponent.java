package dev.isxander.controlify.controller.rumble;

import dev.isxander.controlify.config.settings.profile.RumbleSettings;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class RumbleComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("rumble");

    private RumbleState state = null;
    private final RumbleManager rumbleManager;

    public RumbleComponent() {
        this.rumbleManager = new RumbleManager(this);
    }

    public void queueRumble(RumbleState state) {
        if (settings().enabled) {
            this.state = state;
        }
    }

    public Optional<RumbleState> consumeRumble() {
        RumbleState state = this.state;
        this.state = null;
        return Optional.ofNullable(state);
    }

    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    public RumbleSettings settings() {
        return this.controller().settings().rumble;
    }

    public RumbleSettings defaultSettings() {
        return this.controller().defaultSettings().rumble;
    }

    @Override
    public Identifier id() {
        return ID;
    }

}
