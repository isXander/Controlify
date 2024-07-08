package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = YACLScreen.CategoryTab.class, remap = false)
public class YACLScreenCategoryTabMixin {
    @Shadow @Final
    public Button saveFinishedButton;

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void onConstructCategory(CallbackInfo ci) {
        ButtonGuideApi.addGuideToButton(
                saveFinishedButton,
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.ALWAYS
        );
    }
}
