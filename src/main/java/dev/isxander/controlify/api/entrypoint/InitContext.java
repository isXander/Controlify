package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;

public interface InitContext {

    ControlifyApi controlify();
}
