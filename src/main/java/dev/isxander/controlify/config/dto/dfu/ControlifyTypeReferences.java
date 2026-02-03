package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;

public final class ControlifyTypeReferences {

    public static final DSL.TypeReference USER_STATE = () -> "controlify:user_state";

    private ControlifyTypeReferences() {
    }
}
