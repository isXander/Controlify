package dev.isxander.controlify.mixins.feature.screenkeyboard;

import dev.isxander.controlify.screenkeyboard.KeyboardWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {

    @Unique
    private KeyboardWidget keyboard;

    protected AbstractSignEditScreenMixin(Component title) {
        super(title);
    }
}
