package dev.isxander.controlify.controller.info;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.controller.SingleValueComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class UIDComponent extends SingleValueComponent<ControllerUID> {
    public static final Identifier ID = CUtil.rl("uid");

    public UIDComponent(ControllerUID value) {
        super(value, ID);
    }

    public UIDComponent(String value) {
        super(new ControllerUID(value), ID);
    }
}
