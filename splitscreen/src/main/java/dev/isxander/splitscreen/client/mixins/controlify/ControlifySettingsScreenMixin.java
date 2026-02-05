package dev.isxander.splitscreen.client.mixins.controlify;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.gui.screen.ControlifySettingsScreen;
import dev.isxander.splitscreen.client.host.gui.ControlifySplitscreenSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ControlifySettingsScreen.class)
public abstract class ControlifySettingsScreenMixin extends Screen {
    @Shadow
    @Final
    private @Nullable Screen parent;

    protected ControlifySettingsScreenMixin(Component title) {
        super(title);
    }

    @WrapMethod(method = "openScreen")
    private static ControlifySettingsScreen openSplitscreenSettingsScreen(Screen parent, Operation<ControlifySettingsScreen> original) {
        var screen = new ControlifySplitscreenSettingsScreen(parent);
        Minecraft.getInstance().setScreen(screen);
        return screen;
    }
}
