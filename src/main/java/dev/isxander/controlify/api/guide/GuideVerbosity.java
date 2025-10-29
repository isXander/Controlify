package dev.isxander.controlify.api.guide;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum GuideVerbosity implements StringRepresentable, NameableEnum {
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

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

    public static Codec<GuideVerbosity> CODEC =
            StringRepresentable.fromEnum(GuideVerbosity::values);
}
