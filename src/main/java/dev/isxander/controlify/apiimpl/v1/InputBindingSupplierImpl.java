package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.api.CID;
import dev.isxander.controlify.api.MinecraftComponent;
import dev.isxander.controlify.api.v1.ControlifyController;
import dev.isxander.controlify.api.v1.bindings.InputBinding;
import dev.isxander.controlify.api.v1.bindings.InputBindingSupplier;
import dev.isxander.controlify.apiimpl.APIImplUtil;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

class InputBindingSupplierImpl implements InputBindingSupplier {
    private final dev.isxander.controlify.api.bind.InputBindingSupplier implSupplier;

    public InputBindingSupplierImpl(dev.isxander.controlify.api.bind.InputBindingSupplier implSupplier) {
        this.implSupplier = implSupplier;
    }

    @Override
    public CID bindId() {
        return APIImplUtil.toAPIIdentifier(this.implSupplier.bindId());
    }

    @Override
    public @Nullable InputBinding onOrNull(ControlifyController controller) {
        return new InputBindingImpl(this.implSupplier.onOrNull(((ControlifyControllerImpl) controller).impl()));
    }

    @Override
    public @MinecraftComponent Object glyphIcon() {
        return (Component) this.implSupplier.inputGlyph();
    }

    public dev.isxander.controlify.api.bind.InputBindingSupplier impl() {
        return this.implSupplier;
    }
}
