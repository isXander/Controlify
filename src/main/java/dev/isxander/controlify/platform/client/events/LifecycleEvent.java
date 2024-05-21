package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface LifecycleEvent {
    void onLifecycle(Minecraft minecraft);
}
