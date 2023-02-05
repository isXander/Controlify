package dev.isxander.controlify.bindings;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.gui.ButtonRenderer;

import java.util.Collection;

public interface IBind {
    float state(ControllerState state, Controller controller);
    default boolean held(ControllerState state, Controller controller) {
        return state(state, controller) > controller.config().buttonActivationThreshold;
    }

    void draw(PoseStack matrices, int x, int centerY, Controller controller);
    ButtonRenderer.DrawSize drawSize();

    JsonElement toJson();

    static IBind fromJson(JsonElement json) {
        if (json.isJsonArray()) {
            return new CompoundBind(json.getAsJsonArray().asList().stream().map(element -> Bind.fromIdentifier(element.getAsString())).toArray(Bind[]::new));
        } else {
            return Bind.fromIdentifier(json.getAsString());
        }
    }

    static IBind create(Collection<Bind> binds) {
        if (binds.size() == 1) return binds.stream().findAny().orElseThrow();
        return new CompoundBind(binds.toArray(new Bind[0]));
    }
    static IBind create(Bind... binds) {
        if (binds.length == 1) return binds[0];
        return new CompoundBind(binds);
    }
}
