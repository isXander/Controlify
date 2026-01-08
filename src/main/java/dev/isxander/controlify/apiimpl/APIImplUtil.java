package dev.isxander.controlify.apiimpl;

import dev.isxander.controlify.api.CID;
import net.minecraft.resources.Identifier;

public final class APIImplUtil {
    public static CID toAPIIdentifier(Identifier id) {
        return new CID(id.getNamespace(), id.getPath());
    }

    public static Identifier toMCIdentifier(CID id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }
}
