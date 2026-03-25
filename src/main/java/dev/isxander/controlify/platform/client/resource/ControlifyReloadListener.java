package dev.isxander.controlify.platform.client.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface ControlifyReloadListener extends PreparableReloadListener {
    Identifier getReloadId();
}
