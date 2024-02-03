package dev.isxander.controlify.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.composable.GyroYawMode;
import dev.isxander.controlify.controller.composable.gyro.GyroState;
import dev.isxander.controlify.driver.global.GlobalDriver;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.rumble.RumbleSource;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ControllerConfig implements Serializable, Cloneable {
    public float horizontalLookSensitivity = 1f;
    public float verticalLookSensitivity = 0.9f;

    public float buttonActivationThreshold = 0.5f;

    public float virtualMouseSensitivity = 1f;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;
    public boolean disableFlyDrifting = false;

    public String customName = null;

    public boolean showIngameGuide = true;
    public boolean ingameGuideBottom = false;
    public boolean showScreenGuide = true;

    public boolean showOnScreenKeyboard = GlobalDriver.get().onScreenKeyboard().isOnScreenKeyboardSupported();
    public float chatKeyboardHeight = showOnScreenKeyboard ? 0.5f : 0f;

    public boolean reduceAimingSensitivity = true;

    public Map<ResourceLocation, Float> deadzones = new Object2FloatArrayMap<>();

    public boolean allowVibrations = true;
    public JsonObject vibrationStrengths = RumbleSource.getDefaultJson();

    public boolean deadzonesCalibrated = false;
    public boolean delayedCalibration = false;

    public float gyroLookSensitivity = 0f;
    public boolean relativeGyroMode = false;
    public boolean gyroRequiresButton = true;
    public GyroYawMode gyroYawMode = GyroYawMode.YAW;
    public boolean flickStick = false;
    public boolean invertGyroX = false;
    public boolean invertGyroY = false;
    public GyroState gyroCalibration = new GyroState();

    public boolean mixedInput = false;

    public ResourceLocation[] radialActions = new ResourceLocation[]{
            null, null, null, null, null, null, null, null,
    };

    public boolean dontShowControllerSubmission = false;

    public ControllerConfig(Set<ResourceLocation> deadzoneAxes) {
        for (ResourceLocation axis : deadzoneAxes) {
            deadzones.put(axis, 0.2f);
        }
    }

    public float getRumbleStrength(RumbleSource source) {
        return vibrationStrengths.asMap().getOrDefault(source.id().toString(), new JsonPrimitive(1f)).getAsFloat();
    }

    public void setRumbleStrength(RumbleSource source, float strength) {
        vibrationStrengths.addProperty(source.id().toString(), strength);
    }

    public boolean validateRadialActions(ControllerBindings bindings) {
        boolean changed = false;
        for (int i = 0; i < radialActions.length; i++) {
            ResourceLocation action = radialActions[i];
            if (!RadialMenuScreen.EMPTY_ACTION.equals(action) && (action == null || !bindings.registry().containsKey(action) || bindings.registry().get(action).radialIcon().isEmpty())) {
                setDefaultRadialAction(bindings, i);
                changed = true;
            }
        }
        if (changed)
            Controlify.instance().config().setDirty();

        return !changed;
    }

    private void setDefaultRadialAction(ControllerBindings bindings, int index) {
        radialActions[index] = switch (index) {
            case 0 -> bindings.TOGGLE_HUD_VISIBILITY.id();
            case 1 -> bindings.CHANGE_PERSPECTIVE.id();
            case 2 -> bindings.DROP_STACK.id();
            case 3 -> bindings.OPEN_CHAT.id();
            case 4 -> bindings.SWAP_HANDS.id();
            case 5 -> bindings.PICK_BLOCK.id();
            case 6 -> bindings.TAKE_SCREENSHOT.id();
            case 7 -> bindings.SHOW_PLAYER_LIST.id();
            default -> RadialMenuScreen.EMPTY_ACTION;
        };
    }

    @Override
    public ControllerConfig clone() {
        try {
            ControllerConfig clone = (ControllerConfig) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
