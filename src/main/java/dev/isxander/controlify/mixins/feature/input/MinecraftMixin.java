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
    private final ThreadLocal<Boolean> useNbtPick = ThreadLocal.withInitial(() -> null);

    //? if >=26.1 {
    @Shadow
    protected abstract void pickBlockOrEntity();
    //?} else {
    /*@Shadow
    protected abstract void pickBlock();

    private void pickBlockOrEntity() {
        return pickBlock();
    }
    *///?}

    @Override
    public void controlify$pickBlock() {
        this.useNbtPick.set(false);
        pickBlockOrEntity();
    }

    @Override
    public void controlify$pickBlockWithNbt() {
        this.useNbtPick.set(true);
        pickBlockOrEntity();
    }

    @ModifyExpressionValue(
            //? if >=26.1 {
            method = "pickBlockOrEntity",
            //?} else {
            /*method = "pickBlock",
            *///?}
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.9 {
                    target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"
                    //?} else {
                    /*target = "Lnet/minecraft/client/gui/screens/Screen;hasControlDown()Z"
                    *///?}
            )
    )
    private boolean shouldUseNbtPick(boolean hasControlDown) {
        Boolean useNbtPick = this.useNbtPick.get();
        if (useNbtPick != null) {
            this.useNbtPick.remove();
            return useNbtPick;
        }
        return hasControlDown;
    }
}
