package dev.isxander.controlify.reacharound;

import dev.isxander.controlify.Controlify;
import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;

public enum ReachAroundMode implements NameableEnum {
    OFF((minecraft, controlify) -> false),
    SINGLEPLAYER_ONLY((minecraft, controlify) -> minecraft.isSingleplayer()),
    SINGLEPLAYER_AND_LAN((minecraft, controlify) -> minecraft.isLocalServer()),
    EVERYWHERE((minecraft, controlify) -> true);

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
}
