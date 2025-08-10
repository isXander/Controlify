package dev.isxander.controlify.mixins.feature.screenkeyboard.editbox;

import dev.isxander.controlify.screenkeyboard.KeyboardSupportedMarker;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EditBox.class)
public class EditBoxMixin implements KeyboardSupportedMarker.Mutable {

    @Unique
    private boolean keyboardSupported = false;

    @Override
    public void controlify$setKeyboardSupported(boolean supported) {
        this.keyboardSupported = supported;
    }

    @Override
    public boolean controlify$isKeyboardSupported() {
        return this.keyboardSupported;
    }
}
