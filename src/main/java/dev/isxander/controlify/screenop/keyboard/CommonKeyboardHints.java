package dev.isxander.controlify.screenop.keyboard;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.utils.LazyComponentDims;
import net.minecraft.network.chat.Component;

public final class CommonKeyboardHints {

    public static final LazyComponentDims TEXT_CURSOR = new LazyComponentDims(
            Component.translatable("controlify.hint.keyboard_cursor_movement",
                    BindingFontHelper.binding(ControlifyBindings.GUI_PREV_TAB),
                    BindingFontHelper.binding(ControlifyBindings.GUI_NEXT_TAB))
    );

    public static final LazyComponentDims OPEN_KEYBOARD = new LazyComponentDims(
            Component.translatable("controlify.hint.edit_box_keyboard",
                    BindingFontHelper.binding(ControlifyBindings.GUI_PRESS))
    );

    private CommonKeyboardHints() {}
}
