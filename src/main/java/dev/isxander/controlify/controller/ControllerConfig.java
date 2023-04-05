package dev.isxander.controlify.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.isxander.controlify.rumble.RumbleSource;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public abstract class ControllerConfig {
    public float horizontalLookSensitivity = 1f;
    public float verticalLookSensitivity = 0.9f;

    public float buttonActivationThreshold = 0.5f;

    public int screenRepeatNavigationDelay = 4;

    public float virtualMouseSensitivity = 1f;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;

    public String customName = null;

    public boolean showIngameGuide = true;
    public boolean showScreenGuide = true;

    public float chatKeyboardHeight = 0f;

    public boolean reduceAimingSensitivity = true;

    public boolean allowVibrations = true;
    public JsonObject vibrationStrengths = RumbleSource.getDefaultJson();

    public boolean calibrated = false;

    public abstract void setDeadzone(int axis, float deadzone);
    public abstract float getDeadzone(int axis);

    public float getRumbleStrength(RumbleSource source) {
        return vibrationStrengths.asMap().getOrDefault(source.id().toString(), new JsonPrimitive(1f)).getAsFloat();
    }

    public void setRumbleStrength(RumbleSource source, float strength) {
        vibrationStrengths.addProperty(source.id().toString(), strength);
    }
}
