package dev.isxander.controlify.bindings;

import com.mojang.serialization.Lifecycle;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record BindContext(ResourceLocation id, Function<Minecraft, Boolean> isApplicable) {
    public static final Registry<BindContext> REGISTRY = new MappedRegistry<>(
            ResourceKey.createRegistryKey(new ResourceLocation("controlify", "bind_context")),
            Lifecycle.stable()
    );

    public static final BindContext UNKNOWN = register(
            "unknown",
            mc -> true
    );

    public static final BindContext IN_GAME = register(
            "in_game",
            mc -> mc.screen == null && mc.level != null
    );

    public static final BindContext ANY_SCREEN = register(
            "screen",
            mc -> mc.screen != null
    );

    public static final BindContext REGULAR_SCREEN = register(
            "regular_screen",
            mc -> mc.screen != null
                    && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext CONTAINER = register(
            "container",
            mc -> mc.screen instanceof ContainerScreen
    );

    public static final BindContext V_MOUSE_CURSOR = register(
            "vmouse_cursor",
            mc -> mc.screen != null
                    && ScreenProcessorProvider.provide(mc.screen).virtualMouseBehaviour().hasCursor()
                    && Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext V_MOUSE_COMPAT = register(
            "vmouse_compat",
            mc -> mc.screen != null
                    && ScreenProcessorProvider.provide(mc.screen).virtualMouseBehaviour() == VirtualMouseBehaviour.ENABLED
                    && Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext RADIAL_MENU = register(
            "radial_menu",
            mc -> mc.screen instanceof RadialMenuScreen
    );

    private static BindContext register(String path, Function<Minecraft, Boolean> predicate) {
        var context = new BindContext(new ResourceLocation("controlify", path), predicate);
        Registry.register(REGISTRY, context.id(), context);
        return context;
    }
}
