package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public final class ControlifySchemas {
    private ControlifySchemas() {
    }

    private static class ControlifySchema extends Schema {
        public ControlifySchema(int versionKey, Schema parent) {
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

    public static class SchemaV0 extends ControlifySchema {
        public SchemaV0(int versionKey, Schema parent) {
            super(versionKey, parent);
        }

        @Override
        public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
            return Map.of();
        }

        @Override
        public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
            return Map.of();
        }
    }

    public static class SchemaV1 extends ControlifySchema {
        public SchemaV1(int versionKey, Schema parent) {
            super(versionKey, parent);
        }
    }

    public static class SchemaV2 extends ControlifySchema {
        public SchemaV2(int versionKey, Schema parent) {
            super(versionKey, parent);
        }
    }
}
