package dev.isxander.controlify.bindings;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record BindContext(Identifier id, Function<Minecraft, Boolean> isApplicable) {
    public static final Map<Identifier, BindContext> CONTEXTS = new HashMap<>();

    public static final BindContext UNKNOWN = register(
            "unknown",
            mc -> true
    );

    public static final BindContext IN_GAME = register(
            "in_game",
            mc -> MinecraftUtil.getScreen() == null && mc.level != null && mc.player != null
    );

    public static final BindContext ANY_SCREEN = register(
            "screen",
            mc -> MinecraftUtil.getScreen() != null
    );

    public static final BindContext REGULAR_SCREEN = register(
            "regular_screen",
            mc -> MinecraftUtil.getScreen() != null
                    && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext CONTAINER = register(
            "container",
            mc -> MinecraftUtil.getScreen() instanceof AbstractContainerScreen<?>
    );

    public static final BindContext V_MOUSE_CURSOR = register(
            "vmouse_cursor",
            mc -> MinecraftUtil.getScreen() != null
                    && ScreenProcessorProvider.provide(MinecraftUtil.getScreen()).virtualMouseBehaviour().hasCursor()
                    && Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext V_MOUSE_COMPAT = register(
            "vmouse_compat",
            mc -> MinecraftUtil.getScreen() != null
                    && ScreenProcessorProvider.provide(MinecraftUtil.getScreen()).virtualMouseBehaviour() == VirtualMouseBehaviour.ENABLED
                    && Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext RADIAL_MENU = register(
            "radial_menu",
            mc -> MinecraftUtil.getScreen() instanceof RadialMenuScreen
    );

    private static BindContext register(String path, Function<Minecraft, Boolean> predicate) {
        var context = new BindContext(CUtil.rl(path), predicate);
        CONTEXTS.put(context.id, context);
        return context;
    }
}
