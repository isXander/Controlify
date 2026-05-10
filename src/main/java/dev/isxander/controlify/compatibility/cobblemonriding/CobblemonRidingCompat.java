package dev.isxander.controlify.compatibility.cobblemonriding;

import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.KeyMappingHandle;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class CobblemonRidingCompat {
    private static final String COBBLEMON_NAMESPACE = "cobblemon";

    private static boolean jumpPressed;
    private static boolean sneakPressed;

    public static void init() {
        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(event -> tick(event.controller()));
        ControlifyEvents.INPUT_MODE_CHANGED.register(event -> releaseAll());
    }

    private static void tick(ControllerEntity controller) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldEmulateVanillaKeys(minecraft)) {
            releaseAll();
            return;
        }

        setPressed(minecraft.options.keyJump, ControlifyBindings.JUMP.on(controller).digitalNow(), true);
        setPressed(minecraft.options.keyShift, ControlifyBindings.SNEAK.on(controller).digitalNow(), false);
    }

    private static boolean shouldEmulateVanillaKeys(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null)
            return false;

        Entity vehicle = minecraft.player.getVehicle();
        if (vehicle == null)
            return false;

        Identifier vehicleId = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        return vehicleId != null && COBBLEMON_NAMESPACE.equals(vehicleId.getNamespace());
    }

    private static void releaseAll() {
        Minecraft minecraft = Minecraft.getInstance();
        setPressed(minecraft.options.keyJump, false, true);
        setPressed(minecraft.options.keyShift, false, false);
    }

    private static void setPressed(KeyMapping keyMapping, boolean pressed, boolean jump) {
        boolean wasPressed = jump ? jumpPressed : sneakPressed;
        if (wasPressed == pressed)
            return;

        ((KeyMappingHandle) keyMapping).controlify$setPressed(pressed);

        if (jump) {
            jumpPressed = pressed;
        } else {
            sneakPressed = pressed;
        }
    }

    private CobblemonRidingCompat() {
    }
}
