package dev.isxander.controlify.bindings.v2;

import com.mojang.serialization.Lifecycle;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
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

    public static final BindContext UNKNOWN = new BindContext(
            new ResourceLocation("controlify", "unknown"),
            mc -> true
    );

    public static final BindContext IN_GAME = new BindContext(
            new ResourceLocation("controlify", "in_game"),
            mc -> mc.screen == null && mc.level != null
    );

    public static final BindContext ANY_SCREEN = new BindContext(
            new ResourceLocation("controlify", "screen"),
            mc -> mc.screen != null
    );

    public static final BindContext REGULAR_SCREEN = new BindContext(
            new ResourceLocation("controlify", "screen"),
            mc -> mc.screen != null // TODO
    );

    public static final BindContext CONTAINER = new BindContext(
            new ResourceLocation("controlify", "container"),
            mc -> mc.screen instanceof ContainerScreen
    );

    public static final BindContext V_MOUSE_CURSOR = new BindContext(
            new ResourceLocation("controlify", "vmouse_cursor"),
            mc -> mc.screen != null
                    && ScreenProcessorProvider.provide(mc.screen).virtualMouseBehaviour().hasCursor()
                    && Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()
    );

    public static final BindContext V_MOUSE_COMPAT = new BindContext(
            new ResourceLocation("controlify", "vmouse_compat"),
            mc -> false // TODO
    );

    public static final BindContext RADIAL_MENU = new BindContext(
            new ResourceLocation("controlify", "radial_menu"),
            mc -> mc.screen instanceof RadialMenuScreen
    );

    private static BindContext register(String path, Function<Minecraft, Boolean> predicate) {
        var context = new BindContext(new ResourceLocation("controlify", path), predicate);
        Registry.register(REGISTRY, context.id(), context);
        return context;
    }
}
