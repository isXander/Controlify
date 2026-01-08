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
import dev.isxander.controlify.screenop.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.screenop.keyboard.KeyboardOverlayScreen;
import dev.isxander.controlify.utils.render.CGuiPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget implements ComponentProcessorProvider {

    @Shadow private @Nullable Component hint;
    @Shadow private @Nullable String suggestion;
    @Shadow @Final private Font font;

    @Shadow
    public abstract boolean isBordered();

    @Unique
    private final EditBoxComponentProcessor processor = new EditBoxComponentProcessor(
            (EditBox) (Object) this,
            Minecraft.getInstance().getWindow().getGuiScaledWidth(),
            Minecraft.getInstance().getWindow().getGuiScaledHeight()
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
                && controller.genericConfig().config().showScreenGuides
                && ControlifyApi.get().currentInputMode().isController()
                && !(Minecraft.getInstance().screen instanceof KeyboardOverlayScreen)
                && processor.getKeyboardBehaviour() instanceof ComponentKeyboardBehaviour.Handled
            ) {
                int textX = this.getX() + (this.isBordered() ? 2 : 0) + 2;
                int textY = this.getY() + (this.isBordered() ? 2 : 0) + 4;

                if (renderedValue.isEmpty()
                    && this.hint == null
                    && this.suggestion == null
                ) {
                    var component = CommonKeyboardHints.OPEN_KEYBOARD;
                    int width = component.getWidth();

                    var pose = CGuiPose.ofPush(graphics);
                    if (width > this.getWidth() + (this.isBordered() ? 4 : 0)) {
                        pose.translate(textX, textY + 3);
                        pose.scale(0.5f, 0.5f);
                        pose.translate(-textX, -textY - 3);
                    }

                    renderHint.set(true);
                    graphics.drawString(font, component.getComponent(), textX, textY, 0xFFAAAAAA);

                    pose.pop();
                } else {
                    var component = ControlifyBindings.GUI_PRESS.inputGlyph();
                    int width = font.width(component);

                    graphics.drawString(
                            font, component,
                            this.getRight() - 2 - width,
                            textY,
                            -1
                    );
                }
            }
        });

        return renderedValue;
    }

    @Definition(id = "isFocused", method = "Lnet/minecraft/client/gui/components/EditBox;isFocused()Z")
    @Definition(id = "focusedTime", field = "Lnet/minecraft/client/gui/components/EditBox;focusedTime:J")
    @Expression("(?() - this.focusedTime) / 300 % 2 == 0")
    @ModifyExpressionValue(method = "renderWidget", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean preventShowingCursor(boolean showCursor, @Share("renderHint") LocalBooleanRef renderHint) {
        return showCursor && !renderHint.get();
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return processor;
    }
}
