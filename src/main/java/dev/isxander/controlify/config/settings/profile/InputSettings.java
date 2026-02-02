package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.config.dto.profile.InputConfig;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.ingame.InputCurves;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InputSettings {
    public final BindingsSettings bindings;
    public final SensitivitySettings sensitivity;
    public final RadialMenuSettings radialMenu;
    public float buttonActivationThreshold;
    public boolean keepDefaultBindings;

    public InputSettings(
            BindingsSettings bindings,
            SensitivitySettings sensitivity,
            RadialMenuSettings radialMenu,
            float buttonActivationThreshold,
            boolean keepDefaultBindings
    ) {
        this.bindings = bindings;
        this.sensitivity = sensitivity;
        this.radialMenu = radialMenu;
        this.buttonActivationThreshold = buttonActivationThreshold;
        this.keepDefaultBindings = keepDefaultBindings;
    }

    public static InputSettings fromDTO(InputConfig dto) {
        return new InputSettings(
                BindingsSettings.fromDTO(dto.bindings()),
                SensitivitySettings.fromDTO(dto.sensitivity()),
                RadialMenuSettings.fromDTO(dto.radialMenu()),
                dto.buttonActivationThreshold(),
                dto.keepDefaultBindings()
        );
    }

    public InputConfig toDTO() {
        return new InputConfig(
                bindings.toDTO(),
                sensitivity.toDTO(),
                radialMenu.toDTO(),
                buttonActivationThreshold,
                keepDefaultBindings
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
        public float defaultDeadzone;
        private final Map<Identifier, Float> deadzones;

        public SensitivitySettings(
                float hLookSensitivity,
                float vLookSensitivity,
                boolean vLookInvert,
                float virtualMouseSensitivity,
                boolean reduceAimingSensitivity,
                InputCurves lookInputCurve,
                boolean isLCE,
                float defaultDeadzone,
                Map<Identifier, Float> deadzones
        ) {
            this.hLookSensitivity = hLookSensitivity;
            this.vLookSensitivity = vLookSensitivity;
            this.vLookInvert = vLookInvert;
            this.virtualMouseSensitivity = virtualMouseSensitivity;
            this.reduceAimingSensitivity = reduceAimingSensitivity;
            this.lookInputCurve = lookInputCurve;
            this.isLCE = isLCE;
            this.defaultDeadzone = defaultDeadzone;
            this.deadzones = new HashMap<>(deadzones);
        }

        public float getDeadzone(Identifier deadzoneGroupId) {
            return deadzones.getOrDefault(deadzoneGroupId, defaultDeadzone);
        }

        public void putDeadzone(Identifier deadzoneGroupId, float deadzone) {
            deadzones.put(deadzoneGroupId, deadzone);
        }

        public static SensitivitySettings fromDTO(InputConfig.SensitivityConfig dto) {
            return new SensitivitySettings(
                    dto.hLookSensitivity(),
                    dto.vLookSensitivity(),
                    dto.vLookInvert(),
                    dto.virtualMouseSensitivity(),
                    dto.reduceAimingSensitivity(),
                    dto.lookInputCurve(),
                    dto.isLCE(),
                    dto.defaultDeadzone(),
                    dto.deadzones()
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
                    isLCE,
                    defaultDeadzone,
                    Map.copyOf(deadzones)
            );
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
