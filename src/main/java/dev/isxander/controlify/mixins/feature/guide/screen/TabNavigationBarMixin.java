package dev.isxander.controlify.mixins.feature.guide.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabNavigationBar.class)
public class TabNavigationBarMixin {
    @Shadow @Final private ImmutableList<TabButton> tabButtons;

    @Shadow private int width;

    @Inject(method = "render", at = @At("RETURN"))
    private void renderControllerButtonOverlay(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Controlify.instance().currentInputMode() == InputMode.CONTROLLER) {
            var controller = Controlify.instance().currentController();
            if (controller.config().showScreenGuide) {
                this.renderControllerButtonOverlay(matrices, controller);
            }
        }
    }

    private void renderControllerButtonOverlay(PoseStack matrices, Controller<?, ?> controller) {
        ControlifyCompat.ifBeginHudBatching();

        TabButton firstTab = tabButtons.get(0);
        TabButton lastTab = tabButtons.get(tabButtons.size() - 1);

        BindRenderer prevBind = controller.bindings().GUI_PREV_TAB.renderer();
        DrawSize prevBindDrawSize = prevBind.size();
        int firstButtonX = Math.max(firstTab.getX() - 2 - prevBindDrawSize.width(), firstTab.getX() / 2 - prevBindDrawSize.width() / 2);
        int firstButtonY = 12;
        prevBind.render(matrices, firstButtonX, firstButtonY);

        BindRenderer nextBind = controller.bindings().GUI_NEXT_TAB.renderer();
        DrawSize nextBindDrawSize = nextBind.size();
        int lastButtonEnd = lastTab.getX() + lastTab.getWidth();
        int lastButtonX = Math.min(lastTab.getX() + lastTab.getWidth() + 2, lastButtonEnd + (width - lastButtonEnd) / 2 - nextBindDrawSize.width() / 2);
        int lastButtonY = 12;
        nextBind.render(matrices, lastButtonX, lastButtonY);

        ControlifyCompat.ifEndHudBatching();
    }
}
