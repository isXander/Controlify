package dev.isxander.controlify.controller.composable;

import dev.isxander.controlify.controllermanager.UniqueControllerID;

public interface ComposableControllerInfo {
    String uid();

    UniqueControllerID ucid();

    String guid();

    String createName(ComposableController<?> controller);

    default void close() {}
}
