package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import dev.isxander.controlify.config.dto.dfu.fixes.FixControllersMapToSingleController;

public final class ControlifyDataFixer {
    public static final int CURRENT_VERSION = 1;

    private static final DataFixer FIXER = createFixer();

    public static DataFixer getFixer() {
        return FIXER;
    }

    private static DataFixer createFixer() {
        var builder = new DataFixerBuilder(CURRENT_VERSION);

        var v0 = builder.addSchema(0, ControlifySchemas.SchemaV0::new);
        var v1 = builder.addSchema(1, ControlifySchemas.SchemaV1::new);

        builder.addFixer(new FixControllersMapToSingleController(v1));

        return builder.build().fixer();
    }

    private ControlifyDataFixer() {
    }
}
