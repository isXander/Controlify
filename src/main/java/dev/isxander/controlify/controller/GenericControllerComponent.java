package dev.isxander.controlify.controller;

import dev.isxander.controlify.controller.config.*;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericControllerComponent implements ComponentWithConfig<GenericControllerComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("config/generic");
    public static final ConfigModule<Config> CONFIG_MODULE = new ConfigModule<>(ID, Config.class);

    private final ConfigInstance<Config> config;

    public GenericControllerComponent(ControllerEntity controller) {
        this.config = new ConfigInstanceImpl<>(ID, ModuleRegistry.INSTANCE, controller);
    }

    @Override
    public ConfigInstance<Config> getConfigInstance() {
        return config;
    }

    public static class Config implements ConfigObject {
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
}
