package dev.isxander.controlify.splitscreen.ipc.utils;

import dev.isxander.controlify.controller.ControllerUID;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ExtraStreamCodecs {

    private ExtraStreamCodecs() {}

    public static final StreamCodec<ByteBuf, ControllerUID> CONTROLLER_UID =
            ByteBufCodecs.STRING_UTF8.map(ControllerUID::new, ControllerUID::string);
}
