package dev.isxander.controlify.controller.info;

import dev.isxander.controlify.controller.SingleValueComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class GUIDComponent extends SingleValueComponent<String> {
    public static final Identifier ID = CUtil.rl("guid");

    public GUIDComponent(String value) {
        super(value, ID);
    }
}
