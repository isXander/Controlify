package dev.isxander.controlify.controller.rumble;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.config.*;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class RumbleComponent implements ComponentWithConfig<RumbleComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("rumble");
    public static final ConfigModule<Config> CONFIG_MODULE = new ConfigModule<>(ID, Config.class);

    private RumbleState state = null;
    private final ConfigInstance<Config> config;
    private final RumbleManager rumbleManager;

    public RumbleComponent(ControllerEntity controller) {
        this.config = new ConfigInstanceImpl<>(ID, ModuleRegistry.INSTANCE, controller);
        this.rumbleManager = new RumbleManager(this);
    }

    public void queueRumble(RumbleState state) {
        if (confObj().enabled) {
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

    @Override
    public ConfigInstance<Config> getConfigInstance() {
        return config;
    }

    public static class Config implements ConfigObject {
        public boolean enabled = true;

        public Map<ResourceLocation, Float> vibrationStrengths = RumbleSource.getDefaultMap();

        public RumbleState applyRumbleStrength(RumbleState state, RumbleSource source) {
            float strength = this.getStrength(source);
            if (source != RumbleSource.MASTER) { // don't apply master twice
                strength *= this.getStrength(RumbleSource.MASTER);
            }

            return state.mul(strength);
        }

        private float getStrength(RumbleSource source) {
            return this.vibrationStrengths.getOrDefault(source.id(), 1f);
        }
    }
}
