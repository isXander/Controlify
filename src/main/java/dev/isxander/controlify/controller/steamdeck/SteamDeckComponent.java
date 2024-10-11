package dev.isxander.controlify.controller.steamdeck;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class SteamDeckComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("steam_deck");

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
