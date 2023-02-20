package dev.isxander.controlify.mixins.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlElement.class)
public abstract class ControlElementMixin<T> extends AbstractWidgetMixin {

    @Shadow @Final protected Option<T> option;
    @Shadow @Final protected Dim2i dim;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderFocusRect(PoseStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.isFocused()) {
            int x1 = this.dim.x();
            int y1 = this.dim.y();
            int x2 = this.dim.getLimitX();
            int y2 = this.dim.getLimitY();

            this.drawRect(x1, y1, x2, y1 + 1, -1);
            this.drawRect(x1, y2 - 1, x2, y2, -1);
            this.drawRect(x1, y1, x1 + 1, y2, -1);
            this.drawRect(x2 - 1, y1, x2, y2, -1);;
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/gui/options/control/ControlElement;hovered:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private boolean shouldShortenName(boolean hovered) {
        return hovered || this.isFocused();
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (!this.option.isAvailable())
            return null;
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height());
    }
}
