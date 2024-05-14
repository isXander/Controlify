package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record BindContext(ResourceLocation context, Function<Minecraft, Boolean> isApplicable) {
    public static final BindContext IN_GAME = new BindContext(
            new ResourceLocation("controlify", "in_game"),
            mc -> mc.screen == null && mc.level != null
    );

    public static final BindContext SCREEN = new BindContext(
            new ResourceLocation("controlify", "screen"),
            mc -> mc.screen != null
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
}
