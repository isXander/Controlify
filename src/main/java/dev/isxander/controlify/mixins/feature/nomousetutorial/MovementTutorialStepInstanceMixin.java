package dev.isxander.controlify.mixins.feature.nomousetutorial;

import dev.isxander.controlify.api.ControlifyApi;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceMixin {
    @Shadow
    private int lookCompleted;

    @Shadow
    private int timeLooked;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void stopLookTutorial(CallbackInfo ci) {
        if (ControlifyApi.get().currentInputMode().isController()) {
            this.lookCompleted = 100; // anything other than -1 seems to work
            this.timeLooked = 41; // anything >40
        }
    }
}
