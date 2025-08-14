//? if fabric {
package dev.isxander.controlify.platform.fabric.mixins;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(KeyBindingRegistryImpl.class)
public interface KeyBindingRegistryImplAccessor {
    @Accessor("MODDED_KEY_BINDINGS")
    static List<KeyMapping> getCustomKeys() {
        throw new AssertionError();
    }
}
//?}
