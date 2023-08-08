package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;

public class EditBoxComponentProcessor implements ComponentProcessor {
    @Override
    public boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
        return true;
    }
}
