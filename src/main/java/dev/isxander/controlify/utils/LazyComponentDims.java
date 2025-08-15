package dev.isxander.controlify.utils;

import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class LazyComponentDims {
    private final Component component;
    private final Font font;
    private int width = -1;
    private int height = -1;

    private int cacheKey = -1;

    public LazyComponentDims(Font font, Component component) {
        this.component = component;
        this.font = font;
    }

    public LazyComponentDims(Component component) {
        this(Minecraft.getInstance().font, component);
    }

    public Component getComponent() {
        return this.component;
    }

    public int getWidth() {
        int newCacheKey = getCacheKey();
        if (this.width == -1 || this.cacheKey != newCacheKey) {
            this.cacheKey = newCacheKey;
            this.width = this.font.width(this.component);
        }
        return this.width;
    }

    public int getHeight() {
        int newCacheKey = getCacheKey();
        if (this.height == -1 || this.cacheKey != newCacheKey) {
            this.cacheKey = newCacheKey;
            this.height = BindingFontHelper.getComponentHeight(this.font, this.component);
        }
        return this.height;
    }

    private int getCacheKey() {
        var minecraft = Minecraft.getInstance();
        return Objects.hash(
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(),
                minecraft.font
        );
    }
}
