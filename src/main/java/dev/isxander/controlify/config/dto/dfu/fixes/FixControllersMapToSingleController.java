package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;

import java.util.stream.Stream;

public final class FixControllersMapToSingleController extends DataFix {
    public FixControllersMapToSingleController(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

        return fixTypeEverywhereTyped(
                "Controlify: controllers map to single controller",
                type,
                typed -> typed.update(
                        DSL.remainderFinder(),
                        this::rewrite
                )
        );
    }

    private Dynamic<?> rewrite(Dynamic<?> root) {
        // Read current_controller
        String currentId = root.get("current_controller").asString(null);
        if (currentId == null) {
            return root;
        }

        // Read controllers map
        Dynamic<?> controllers = root.get("controllers").orElseEmptyMap();

        // Pull the entry matching current_controller
        Dynamic<?> controller = controllers.get(currentId).orElseEmptyMap();

        // If either field is missing, leave unchanged
        if (controller.getValue() == null) {
            return root;
        }

        // Remove old controllers map
        root = root.remove("controllers");
        // Insert new controller list
        var controllersList = root.createList(Stream.of(controller));
        root = root.set("controllers", controllersList);

        return root;
    }
}
