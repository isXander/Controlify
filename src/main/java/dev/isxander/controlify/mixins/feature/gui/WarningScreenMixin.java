package dev.isxander.controlify.mixins.feature.gui;

import dev.isxander.controlify.gui.screen.CustomWarningTitlePadding;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WarningScreen.class)
public class WarningScreenMixin implements CustomWarningTitlePadding {
    @ModifyConstant(method = "render", constant = @Constant(intValue = 70))
    private int modifyMessageY(int original) {
        return this.getMessageY();
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 76))
    private int modifyCheckboxY(int original) {
        return this.getMessageY() + 6;
    }
}
