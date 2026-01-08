package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.api.v1.ControlifyController;
import dev.isxander.controlify.controller.ControllerEntity;

record ControlifyControllerImpl(ControllerEntity impl) implements ControlifyController {
    @Override
    public String uid() {
        return impl().uid();
    }
}
