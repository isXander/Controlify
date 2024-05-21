package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface TickEvent {
    void onTick(Minecraft minecraft);
}
