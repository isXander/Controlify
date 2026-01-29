package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.config.dto.controller.InputConfig;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.ingame.InputCurves;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InputSettings {
    public BindingsSettings bindings;
    public SensitivitySettings sensitivity;
    public RadialMenuSettings radialMenu;
    public CalibrationSettings calibration;
    public float buttonActivationThreshold;
    public Map<Identifier, Float> deadzones;
    public boolean mixedInput;
    public boolean keepDefaultBindings;
    public @Nullable ControllerMapping mapping;

    public InputSettings(
            BindingsSettings bindings,
            SensitivitySettings sensitivity,
            RadialMenuSettings radialMenu,
            CalibrationSettings calibration,
            float buttonActivationThreshold,
            Map<Identifier, Float> deadzones,
            boolean mixedInput,
            boolean keepDefaultBindings,
            @Nullable ControllerMapping mapping
    ) {
        this.bindings = bindings;
        this.sensitivity = sensitivity;
        this.radialMenu = radialMenu;
        this.calibration = calibration;
        this.buttonActivationThreshold = buttonActivationThreshold;
        this.deadzones = new HashMap<>(deadzones);
        this.mixedInput = mixedInput;
        this.keepDefaultBindings = keepDefaultBindings;
        this.mapping = mapping;
    }

    public static InputSettings fromDTO(InputConfig dto) {
        return new InputSettings(
                BindingsSettings.fromDTO(dto.bindings()),
                SensitivitySettings.fromDTO(dto.sensitivity()),
                RadialMenuSettings.fromDTO(dto.radialMenu()),
                CalibrationSettings.fromDTO(dto.calibration()),
                dto.buttonActivationThreshold(),
                new HashMap<>(dto.deadzones()),
                dto.mixedInput(),
                dto.keepDefaultBindings(),
                dto.mapping().orElse(null)
        );
    }

    public InputConfig toDTO() {
        return new InputConfig(
                bindings.toDTO(),
                sensitivity.toDTO(),
                radialMenu.toDTO(),
                calibration.toDTO(),
                buttonActivationThreshold,
                Map.copyOf(deadzones),
                mixedInput,
                keepDefaultBindings,
                Optional.ofNullable(mapping)
        );
    }

    public static class BindingsSettings {
        public Map<Identifier, Input> bindings;

        public BindingsSettings(Map<Identifier, Input> bindings) {
            this.bindings = new HashMap<>(bindings);
        }

        public static BindingsSettings fromDTO(InputConfig.BindingsConfig dto) {
            return new BindingsSettings(dto.bindings());
        }

        public InputConfig.BindingsConfig toDTO() {
            return new InputConfig.BindingsConfig(Map.copyOf(bindings));
        }
    }

    public static class SensitivitySettings {
        public float hLookSensitivity;
        public float vLookSensitivity;
        public boolean vLookInvert;
        public float virtualMouseSensitivity;
        public boolean reduceAimingSensitivity;
        public InputCurves lookInputCurve;
        public boolean isLCE;

        public SensitivitySettings(
                float hLookSensitivity,
                float vLookSensitivity,
                boolean vLookInvert,
                float virtualMouseSensitivity,
                boolean reduceAimingSensitivity,
                InputCurves lookInputCurve,
                boolean isLCE
        ) {
            this.hLookSensitivity = hLookSensitivity;
            this.vLookSensitivity = vLookSensitivity;
            this.vLookInvert = vLookInvert;
            this.virtualMouseSensitivity = virtualMouseSensitivity;
            this.reduceAimingSensitivity = reduceAimingSensitivity;
            this.lookInputCurve = lookInputCurve;
            this.isLCE = isLCE;
        }

        public static SensitivitySettings fromDTO(InputConfig.SensitivityConfig dto) {
            return new SensitivitySettings(
                    dto.hLookSensitivity(),
                    dto.vLookSensitivity(),
                    dto.vLookInvert(),
                    dto.virtualMouseSensitivity(),
                    dto.reduceAimingSensitivity(),
                    dto.lookInputCurve(),
                    dto.isLCE()
            );
        }

        public InputConfig.SensitivityConfig toDTO() {
            return new InputConfig.SensitivityConfig(
                    hLookSensitivity,
                    vLookSensitivity,
                    vLookInvert,
                    virtualMouseSensitivity,
                    reduceAimingSensitivity,
                    lookInputCurve,
                    isLCE
            );
        }
    }

    public static class CalibrationSettings {
        public boolean deadzonesCalibrated;
        public boolean delayedCalibration;

        public CalibrationSettings(boolean deadzonesCalibrated, boolean delayedCalibration) {
            this.deadzonesCalibrated = deadzonesCalibrated;
            this.delayedCalibration = delayedCalibration;
        }

        public static CalibrationSettings fromDTO(InputConfig.CalibrationConfig dto) {
            return new CalibrationSettings(dto.deadzonesCalibrated(), dto.delayedCalibration());
        }

        public InputConfig.CalibrationConfig toDTO() {
            return new InputConfig.CalibrationConfig(deadzonesCalibrated, delayedCalibration);
        }
    }

    public static class RadialMenuSettings {
        public List<Identifier> radialActions;
        public int radialButtonFocusTimeoutTicks;

        public RadialMenuSettings(List<Identifier> radialActions, int radialButtonFocusTimeoutTicks) {
            if (radialActions.size() != 8) {
                throw new IllegalArgumentException("radialActions must have exactly 8 elements");
            }
            this.radialActions = List.copyOf(radialActions);
            this.radialButtonFocusTimeoutTicks = radialButtonFocusTimeoutTicks;
        }

        public static RadialMenuSettings fromDTO(InputConfig.RadialMenuConfig dto) {
            return new RadialMenuSettings(dto.radialActions(), dto.radialButtonFocusTimeoutTicks());
        }

        public InputConfig.RadialMenuConfig toDTO() {
            return new InputConfig.RadialMenuConfig(radialActions, radialButtonFocusTimeoutTicks);
        }
    }
}
