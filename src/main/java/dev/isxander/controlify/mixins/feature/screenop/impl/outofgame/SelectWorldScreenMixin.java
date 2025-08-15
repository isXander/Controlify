package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.SelectWorldScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final SelectWorldScreenProcessor processor = new SelectWorldScreenProcessor((SelectWorldScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "GUI_BACK", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Expression("builder(GUI_BACK, ?).?(?, ?, ?, ?).build()")
    @ModifyExpressionValue(method = "init()V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button modifyCancelButton(Button button) {
        ButtonGuideApi.addGuideToButton(
                button,
                ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.always()
        );
        return button;
    }

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Expression("builder(translatable('selectWorld.create'), ?).?(?, ?, ?, ?).build()")
    @ModifyExpressionValue(method = "init()V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button modifyCreateButton(Button button) {
        ButtonGuideApi.addGuideToButton(
                button,
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always()
        );
        return button;
    }
}
