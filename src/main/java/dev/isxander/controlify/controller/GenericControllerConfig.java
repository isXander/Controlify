package dev.isxander.controlify.controller;

import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericControllerConfig implements ConfigClass {
    public static final ResourceLocation ID = CUtil.rl("config/generic");

    @Nullable
    public String nickname = null;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;
    public boolean disableFlyDrifting = false;

    public boolean showIngameGuide = true;
    public boolean ingameGuideBottom = false;
    public boolean showScreenGuides = true;

    public boolean showOnScreenKeyboard = true;

    public boolean dontShowControllerSubmission = false;
}
