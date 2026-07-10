package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;

public final class AddAutoSwitchControllersFix extends DataFix {
    public AddAutoSwitchControllersFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

        return fixTypeEverywhereTyped(
                "Controlify: add auto_switch_controllers and preferred_controller_uid defaults",
                type,
                typed -> typed.update(
                        DSL.remainderFinder(),
                        this::rewrite
                )
        );
    }

    private <T> Dynamic<T> rewrite(Dynamic<T> root) {
        Dynamic<T> global = root.get("global").orElseEmptyMap();

        if (global.get("auto_switch_controllers").result().isEmpty()) {
            global = global.set("auto_switch_controllers", root.createBoolean(true));
        }
        if (global.get("preferred_controller_uid").result().isEmpty()) {
            global = global.set("preferred_controller_uid", root.createString(""));
        }

        return root.set("global", global);
    }
}
