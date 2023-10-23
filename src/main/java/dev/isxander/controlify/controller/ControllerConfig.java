package dev.isxander.controlify.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.resources.ResourceLocation;

import java.io.Serializable;

public abstract class ControllerConfig implements Serializable {
    public float horizontalLookSensitivity = 1f;
    public float verticalLookSensitivity = 0.9f;

    public float buttonActivationThreshold = 0.5f;

    public float virtualMouseSensitivity = 1f;

    public float dpadMoveInterval = 0.3f;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;
    public boolean disableFlyDrifting = false;

    public String customName = null;

    public boolean showIngameGuide = true;
    public boolean ingameGuideBottom = false;
    public boolean showScreenGuide = true;

    public float chatKeyboardHeight = 0f;

    public boolean reduceAimingSensitivity = true;

    public boolean allowVibrations = true;
    public JsonObject vibrationStrengths = RumbleSource.getDefaultJson();

    public boolean deadzonesCalibrated = false;
    public boolean delayedCalibration = false;

    public boolean mixedInput = false;

    public ResourceLocation[] radialActions = new ResourceLocation[]{
            null, null, null, null, null, null, null, null,
    };

    public boolean dontShowControllerSubmission = false;

    public abstract void setDeadzone(int axis, float deadzone);
    public abstract float getDeadzone(int axis);

    public float getRumbleStrength(RumbleSource source) {
        return vibrationStrengths.asMap().getOrDefault(source.id().toString(), new JsonPrimitive(1f)).getAsFloat();
    }

    public void setRumbleStrength(RumbleSource source, float strength) {
        vibrationStrengths.addProperty(source.id().toString(), strength);
    }

    public boolean validateRadialActions(ControllerBindings<?> bindings) {
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

    private void setDefaultRadialAction(ControllerBindings<?> bindings, int index) {
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
}
