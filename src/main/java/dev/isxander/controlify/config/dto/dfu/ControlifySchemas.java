package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public final class ControlifySchemas {
    private ControlifySchemas() {
    }

    public static class SchemaV0 extends Schema {
        public SchemaV0(int versionKey, Schema parent) {
            super(versionKey, parent);
        }

        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
            schema.registerType(
                    true,
                    ControlifyTypeReferences.USER_STATE,
                    DSL::remainder
            );
        }
    }

    public static class SchemaV1 extends SchemaV0 {
        public SchemaV1(int versionKey, Schema parent) {
            super(versionKey, parent);
        }
    }
}
