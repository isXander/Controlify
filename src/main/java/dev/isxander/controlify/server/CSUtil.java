package dev.isxander.controlify.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class CSUtil {
    public static void writeIdentifier(FriendlyByteBuf buf, Identifier identifier) {
        //? if >=1.21.11 {
        buf.writeIdentifier(identifier);
        //?} else {
        /*buf.writeResourceLocation(identifier);
        *///?}
    }

    public static Identifier readIdentifier(FriendlyByteBuf buf) {
        //? if >=1.21.11 {
        return buf.readIdentifier();
        //?} else {
        /*return buf.readResourceLocation();
        *///?}
    }
}
