package dev.isxander.controlify.controller.steamdeck;

import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class SteamDeckComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("steam_deck");

    @Override
    public Identifier id() {
        return ID;
    }
}
