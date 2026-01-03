package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.OptionsScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin implements ScreenProcessorProvider {
    @Unique
    private Button doneButton;

    @Unique
    private final OptionsScreenProcessor screenProcessor = new OptionsScreenProcessor(
            (OptionsScreen) (Object) this,
            () -> this.doneButton
    );

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "GUI_DONE", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_DONE:Lnet/minecraft/network/chat/Component;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Expression("builder(GUI_DONE, ?).?(?).build()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button captureDoneButton(Button button) {
        this.doneButton = button;
        return button;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }
}
