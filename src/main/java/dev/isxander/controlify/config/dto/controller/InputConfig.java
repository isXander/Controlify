package dev.isxander.controlify.config.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.ingame.InputCurves;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record InputConfig(
        BindingsConfig bindings,
        SensitivityConfig sensitivity,
        RadialMenuConfig radialMenu,
        CalibrationConfig calibration,
        float buttonActivationThreshold,
        Map<Identifier, Float> deadzones,
        boolean mixedInput,
        boolean keepDefaultBindings,
        Optional<ControllerMapping> mapping
) {
    public static final Codec<InputConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BindingsConfig.CODEC.optionalFieldOf("bindings", BindingsConfig.EMPTY).forGetter(InputConfig::bindings),
            SensitivityConfig.CODEC.fieldOf("sensitivity").forGetter(InputConfig::sensitivity),
            RadialMenuConfig.CODEC.fieldOf("radial_menu").forGetter(InputConfig::radialMenu),
            CalibrationConfig.CODEC.fieldOf("calibration").forGetter(InputConfig::calibration),
            Codec.floatRange(0.01f, 1f).fieldOf("button_activation_threshold").forGetter(InputConfig::buttonActivationThreshold),
            Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).optionalFieldOf("deadzones", Map.of()).forGetter(InputConfig::deadzones),
            Codec.BOOL.fieldOf("mixed_input").forGetter(InputConfig::mixedInput),
            Codec.BOOL.fieldOf("keep_default_bindings").forGetter(InputConfig::keepDefaultBindings),
            ControllerMapping.CODEC.optionalFieldOf("mapping").forGetter(InputConfig::mapping)
    ).apply(instance, InputConfig::new));

    public record BindingsConfig(
            Map<Identifier, Input> bindings
    ) {
        public static final BindingsConfig EMPTY = new BindingsConfig(Map.of());

        public static final Codec<BindingsConfig> CODEC = Codec.unboundedMap(Identifier.CODEC, Input.CODEC).fieldOf("bindings")
                .codec()
                .xmap(BindingsConfig::new, BindingsConfig::bindings);

        public BindingsConfig {
            bindings = Map.copyOf(bindings);
        }
    }

    public record SensitivityConfig(
        float hLookSensitivity,
        float vLookSensitivity,
        boolean vLookInvert,
        float virtualMouseSensitivity,
        boolean reduceAimingSensitivity,
        InputCurves lookInputCurve,
        boolean isLCE
    ) {
        public static final Codec<SensitivityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("horizontal").forGetter(SensitivityConfig::hLookSensitivity),
                Codec.FLOAT.fieldOf("vertical").forGetter(SensitivityConfig::vLookSensitivity),
                Codec.BOOL.fieldOf("vertical_invert").forGetter(SensitivityConfig::vLookInvert),
                Codec.FLOAT.fieldOf("virtual_mouse").forGetter(SensitivityConfig::virtualMouseSensitivity),
                Codec.BOOL.fieldOf("reduce_aiming").forGetter(SensitivityConfig::reduceAimingSensitivity),
                InputCurves.CODEC.fieldOf("look_curve").forGetter(SensitivityConfig::lookInputCurve),
                Codec.BOOL.fieldOf("is_lce").forGetter(SensitivityConfig::isLCE)
        ).apply(instance, SensitivityConfig::new));
    }

    public record CalibrationConfig(
            boolean deadzonesCalibrated,
            boolean delayedCalibration
    ) {
        public static final Codec<CalibrationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("deadzones_calibrated").forGetter(CalibrationConfig::deadzonesCalibrated),
                Codec.BOOL.fieldOf("delayed_calibration").forGetter(CalibrationConfig::delayedCalibration)
        ).apply(instance, CalibrationConfig::new));
    }

    public record RadialMenuConfig(
            List<Identifier> radialActions,
            int radialButtonFocusTimeoutTicks
    ) {
        public static final Codec<RadialMenuConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.listOf(8, 8).fieldOf("actions").forGetter(RadialMenuConfig::radialActions),
                Codec.INT.fieldOf("button_focus_timeout_ticks").forGetter(RadialMenuConfig::radialButtonFocusTimeoutTicks)
        ).apply(instance, RadialMenuConfig::new));

        public RadialMenuConfig {
            if (radialActions.size() != 8) {
                throw new IllegalArgumentException("radialActions must have exactly 8 elements");
            }
        }
    }
}
