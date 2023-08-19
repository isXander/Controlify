package dev.isxander.controlify.mixins.feature.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.ingame.PickBlockAccessor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements PickBlockAccessor {
    @Unique
    private boolean useNbtPick;

    @Shadow
    protected abstract void pickBlock();

    @Override
    public void controlify$pickBlock() {
        useNbtPick = false;
        pickBlock();
    }

    @Override
    public void controlify$pickBlockWithNbt() {
        useNbtPick = true;
        pickBlock();
    }

    @ModifyExpressionValue(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasControlDown()Z"))
    private boolean shouldUseNbtPick(boolean hasControlDown) {
        if (useNbtPick) {
            useNbtPick = false;
            return true;
        }
        return hasControlDown;
    }
}
