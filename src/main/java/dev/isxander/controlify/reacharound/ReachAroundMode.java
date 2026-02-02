package dev.isxander.controlify.reacharound;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.Controlify;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.function.BiFunction;

public enum ReachAroundMode implements NameableEnum, StringRepresentable {
    OFF((minecraft, controlify) -> false),
    SINGLEPLAYER_ONLY((minecraft, controlify) -> minecraft.isSingleplayer()),
    SINGLEPLAYER_AND_LAN((minecraft, controlify) -> minecraft.isLocalServer()),
    EVERYWHERE((minecraft, controlify) -> true);

    public static final Codec<ReachAroundMode> CODEC = StringRepresentable.fromEnum(ReachAroundMode::values);

    private final BiFunction<Minecraft, Controlify, Boolean> canReachAround;
    private final Component displayName;

    ReachAroundMode(BiFunction<Minecraft, Controlify, Boolean> canReachAround) {
        this.canReachAround = canReachAround;
        this.displayName = Component.translatable("controlify.reach_around." + this.name().toLowerCase());
    }

    public boolean canReachAround() {
        return canReachAround.apply(Minecraft.getInstance(), Controlify.instance());
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name().toLowerCase();
    }
}
