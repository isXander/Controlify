package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.EditBoxComponentProcessor;
import dev.isxander.controlify.screenop.keyboard.ComponentKeyboardBehaviour;
import dev.isxander.controlify.screenop.keyboard.KeyboardOverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget implements ComponentProcessorProvider {

    @Shadow private @Nullable Component hint;
    @Shadow private @Nullable String suggestion;
    @Shadow private int textX;
    @Shadow private int textY;
    @Shadow @Final private Font font;

    @Unique private final EditBoxComponentProcessor processor = new EditBoxComponentProcessor(
            (EditBox) (Object) this,
            Minecraft.getInstance().getWindow().getGuiScaledWidth(),
            Minecraft.getInstance().getWindow().getGuiScaledHeight()
    );
    @Unique private static final Component HINT_TEXT = Component.translatable(
            "controlify.hint.edit_box_keyboard",
            BindingFontHelper.binding(ControlifyBindings.GUI_PRESS)
    );

    public EditBoxMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /**
     * Renders some hint text when the edit box is focused to indicate
     * that pressing GUI_PRESS will open the on-screen keyboard.
     * If the edit box has some text, the hint will be minimally rendered
     */
    @ModifyExpressionValue(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;plainSubstrByWidth(Ljava/lang/String;I)Ljava/lang/String;"))
    private String renderHintText(String renderedValue, @Local(argsOnly = true) GuiGraphics graphics, @Share("renderHint") LocalBooleanRef renderHint) {
        renderHint.set(false);

        ControlifyApi.get().getCurrentController().ifPresent(controller -> {
            if (this.isFocused()
                && controller.genericConfig().config().showOnScreenKeyboard
                && ControlifyApi.get().currentInputMode().isController()
                && !(Minecraft.getInstance().screen instanceof KeyboardOverlayScreen)
                && processor.getKeyboardBehaviour() instanceof ComponentKeyboardBehaviour.Handled
            ) {
                if (renderedValue.isEmpty()
                    && this.hint == null
                    && this.suggestion == null
                ) {
                    renderHint.set(true);
                    graphics.drawString(font, HINT_TEXT, this.textX, this.textY, 0xFFAAAAAA);
                } else {
                    var component = BindingFontHelper.binding(ControlifyBindings.GUI_PRESS);
                    int width = font.width(component);

                    graphics.drawString(
                            font, component,
                            this.getX() - 2 - width,
                            this.textY,
                            -1
                    );
                }
            }
        });

        return renderedValue;
    }

    @Definition(id = "isFocused", method = "Lnet/minecraft/client/gui/components/EditBox;isFocused()Z")
    @Definition(id = "getMillis", method = "Lnet/minecraft/Util;getMillis()J")
    @Definition(id = "focusedTime", field = "Lnet/minecraft/client/gui/components/EditBox;focusedTime:J")
    @Expression("(getMillis() - this.focusedTime) / 300 % 2 == 0")
    @ModifyExpressionValue(method = "renderWidget", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean preventShowingCursor(boolean showCursor, @Share("renderHint") LocalBooleanRef renderHint) {
        return showCursor && !renderHint.get();
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return processor;
    }
}
