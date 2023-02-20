package dev.isxander.controlify.mixins.compat.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.compatibility.sodium.ButtonProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlatButtonWidget.class)
public abstract class FlatButtonWidgetMixin extends AbstractWidgetMixin implements ComponentProcessorProvider {
    @Shadow @Final private Dim2i dim;
    @Shadow private boolean visible;
    @Shadow private boolean enabled;
    @Shadow @Final private Runnable action;

    @Unique private final ComponentProcessor controlify$componentProcessor
            = new ButtonProcessor(() -> action.run());

    @Inject(method = "render", at = @At("TAIL"))
    private void renderFocusRect(PoseStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.enabled && this.isFocused()) {
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

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (!visible || !enabled)
            return null;
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height());
    }

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$componentProcessor;
    }
}
