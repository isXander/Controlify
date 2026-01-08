package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.api.CID;
import dev.isxander.controlify.api.MinecraftComponent;
import dev.isxander.controlify.api.v1.bindings.InputBinding;
import dev.isxander.controlify.apiimpl.APIImplUtil;
import net.minecraft.network.chat.Component;

class InputBindingImpl implements InputBinding {
    private final dev.isxander.controlify.api.bind.InputBinding implBinding;
    private final CID id;

    public InputBindingImpl(dev.isxander.controlify.api.bind.InputBinding implBinding) {
        this.implBinding = implBinding;
        this.id = APIImplUtil.toAPIIdentifier(implBinding.id());
    }

    @Override
    public CID id() {
        return this.id;
    }

    @Override
    public @MinecraftComponent Object glyphIcon() {
        return (Component) this.implBinding.inputGlyph();
    }

    @Override
    public float analogueNow() {
        return this.implBinding.analogueNow();
    }

    @Override
    public float analoguePrev() {
        return this.implBinding.analoguePrev();
    }

    @Override
    public boolean digitalNow() {
        return this.implBinding.digitalNow();
    }

    @Override
    public boolean digitalPrev() {
        return this.implBinding.digitalPrev();
    }

    @Override
    public boolean justPressed() {
        return this.implBinding.justPressed();
    }

    @Override
    public boolean justReleased() {
        return this.implBinding.justReleased();
    }

    @Override
    public boolean justTapped() {
        return this.implBinding.justTapped();
    }

    @Override
    public boolean guiPressed() {
        return this.implBinding.guiPressed().get();
    }
}
