package dev.isxander.controlify.controller.rumble;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class RumbleComponent implements ECSComponent, ConfigHolder<RumbleComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("rumble");

    private RumbleState state = null;
    private final IConfig<Config> config;
    private final RumbleManager rumbleManager;

    public RumbleComponent() {
        this.config = new ConfigImpl<>(Config::new, Config.class);
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
    public IConfig<Config> config() {
        return this.config;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean enabled;
        public Map<ResourceLocation, Float> vibrationStrengths;

        private static final Codec<Map<ResourceLocation, Float>> vibrationStrengthsCodec = Codec.simpleMap(
                ResourceLocation.CODEC,
                Codec.FLOAT,
                Keyable.forStrings(() -> RumbleSource.values().stream().map(s -> s.id().toString()))
        ).codec();

        @Override
        public void save(ValueOutput output, ControllerEntity controller) {
            output.putBoolean("enabled", enabled);
            output.put("strengths", vibrationStrengthsCodec, vibrationStrengths);
        }

        @Override
        public void load(ValueInput input, ControllerEntity controller) {
            this.enabled = input.readBooleanOr("enabled", true);
            this.vibrationStrengths = input.readOr("strengths", vibrationStrengthsCodec, RumbleSource.getDefaultMap());
        }

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
