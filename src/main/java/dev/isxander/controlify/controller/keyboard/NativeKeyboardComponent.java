package dev.isxander.controlify.controller.keyboard;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class NativeKeyboardComponent implements ECSComponent, ConfigHolder<NativeKeyboardComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("native_keyboard");

    private final ConfigImpl<Config> config = new ConfigImpl<>(Config::new, Config.class);

    private final Runnable onOpen;
    private final float keyboardHeight;

    public NativeKeyboardComponent(Runnable onOpen, float keyboardHeight) {
        this.onOpen = onOpen;
        this.keyboardHeight = keyboardHeight;
    }

    public void open() {
        this.onOpen.run();
    }

    public float getKeyboardHeight() {
        return this.keyboardHeight;
    }

    @Override
    public IConfig<Config> config() {
        return config;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean useNativeKeyboard = false;
    }
}
