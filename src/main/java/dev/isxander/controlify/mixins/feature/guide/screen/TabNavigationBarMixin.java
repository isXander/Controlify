package dev.isxander.controlify.mixins.feature.guide.screen;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabNavigationBar.class)
public class TabNavigationBarMixin {
    @Shadow @Final private ImmutableList<TabButton> tabButtons;

    @Inject(method = "render", at = @At("RETURN"))
    private void renderControllerButtonOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Controlify.instance().currentInputMode().isController()) {
            Controlify.instance().getCurrentController().ifPresent(c -> {
                if (c.genericConfig().config().showScreenGuides) {
                    this.renderControllerButtonOverlay(graphics, c);
                }
            });
        }
    }

    @Unique
    private void renderControllerButtonOverlay(GuiGraphics graphics, ControllerEntity controller) {
        if (tabButtons.size() <= 1) return; // no controls necessary

        TabButton firstTab = tabButtons.get(0);
        TabButton lastTab = tabButtons.get(tabButtons.size() - 1);

        Font font = Minecraft.getInstance().font;

        Component prevTabText = BindingFontHelper.binding(controller.bindings().GUI_PREV_TAB);
        int prevTabTextWidth = font.width(prevTabText);
        graphics.drawString(font, prevTabText, firstTab.getX() - 2 - prevTabTextWidth, firstTab.getY() / 2 + font.lineHeight / 2, 0xFFFFFF);

        Component nextTabText = BindingFontHelper.binding(controller.bindings().GUI_NEXT_TAB);
        graphics.drawString(font, nextTabText, lastTab.getX() + lastTab.getWidth() + 2, lastTab.getY() / 2 + font.lineHeight / 2, 0xFFFFFF);
    }
}
