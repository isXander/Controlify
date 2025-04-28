package dev.isxander.controlify.splitscreen.mixins.screenop;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.screenop.PawnSplitscreenModeRegistry;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    /**
     * Fullscreen screens in-game should not render the first pawn's game background,
     * as it breaks the illusion of this fake splitscreen. Instead, render the panorama.
     * @param hasNoLevel original condition to render panorama, if not in game
     * @return if the screen should render the panorama
     */
    @Definition(id = "level", field = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;")
    @Expression("?.level == null")
    @ModifyExpressionValue(method = "renderBackground", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean shouldRenderPanorama(boolean hasNoLevel) {
        boolean isSplitscreen = SplitscreenBootstrapper.isSplitscreen();
        boolean inFullscreenMode = PawnSplitscreenModeRegistry.getMode((Screen) (Object) this) == ScreenSplitscreenMode.FULLSCREEN;

        return hasNoLevel || (isSplitscreen && inFullscreenMode);
    }
}
