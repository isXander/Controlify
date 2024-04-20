package dev.isxander.controlify.mixins.feature.font;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(KeybindContents.class)
public class KeybindContentsMixin {
    @Shadow
    @Final
    private String name;

    @WrapOperation(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/contents/KeybindContents;getNestedComponent()Lnet/minecraft/network/chat/Component;"))
    private Component testVisitWithStyle(KeybindContents instance, Operation<Component> original, @Local(argsOnly = true)Style style) {
        if (BindingFontHelper.WRAPPER_FONT.equals(style.getFont())) {
            Optional<Component> inputText = ControlifyApi.get().getCurrentController().map(c ->
                    Controlify.instance().inputFontMapper()
                            .getComponentFromBinding(
                                    c.info().type().namespace(),
                                    c.bindings().get(ResourceLocation.tryParse(this.name))
                            )
            );
            if (inputText.isPresent()) {
                return inputText.get();
            }
        }

        return original.call(instance);
    }
}
