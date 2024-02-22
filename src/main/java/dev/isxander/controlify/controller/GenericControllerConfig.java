package dev.isxander.controlify.controller;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.driver.global.GlobalDriver;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericControllerConfig {
    public static final ResourceLocation ID = Controlify.id("config/generic");

    @Nullable
    public String nickname = null;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;
    public boolean disableFlyDrifting = false;

    public boolean showIngameGuide = true;
    public boolean ingameGuideBottom = false;
    public boolean showScreenGuides = true;

    public boolean showOnScreenKeyboard = GlobalDriver.get().onScreenKeyboard().isOnScreenKeyboardSupported();
    public float chatKeyboardHeight = showOnScreenKeyboard ? 0.5f : 0f;

    public ResourceLocation[] radialActions = new ResourceLocation[8];

    public boolean dontShowControllerSubmission = false;
}
