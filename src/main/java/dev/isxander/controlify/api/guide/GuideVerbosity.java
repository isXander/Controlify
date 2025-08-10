package dev.isxander.controlify.api.guide;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum GuideVerbosity implements NameableEnum {
    FULL(3),
    REDUCED(2),
    MINIMAL(1);
    
    private final int level;
    
    GuideVerbosity(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.guide_verbosity." + name().toLowerCase());
    }
}
