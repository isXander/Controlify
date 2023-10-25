package dev.isxander.controlify.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.network.chat.Component;

public class FormattableStringController extends StringController {
    private final ValueFormatter<String> formatter;

    /**
     * Constructs a string controller
     *
     * @param option bound option
     * @param formatter the formatter to use
     */
    public FormattableStringController(Option<String> option, ValueFormatter<String> formatter) {
        super(option);
        this.formatter = formatter;
    }

    @Override
    public Component formatValue() {
        return formatter.format(getString());
    }
}
