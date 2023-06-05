package dev.isxander.controlify.mixins.compat.yacl;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(YACLScreen.CategoryTab.class)
public class YACLScreenCategoryTabMixin {
    @Shadow @Final private Button saveFinishedButton;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructCategory(YACLScreen this$0, ConfigCategory category, CallbackInfo ci) {
        ButtonGuideApi.addGuideToButton(saveFinishedButton, bindings -> bindings.GUI_ABSTRACT_ACTION_1, ButtonRenderPosition.TEXT, ButtonGuidePredicate.ALWAYS);
    }
}
