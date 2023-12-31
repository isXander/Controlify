package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.*;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

public class GamepadBind implements IBind<GamepadState> {
    private final Function<GamepadState, Float> stateSupplier;
    private final String identifier;
    private final GamepadLike<?> gamepad;

    public GamepadBind(Function<GamepadState, Float> stateSupplier, String identifier, GamepadLike<?> gamepad) {
        this.stateSupplier = stateSupplier;
        this.identifier = identifier;
        this.gamepad = gamepad;
    }

    @Override
    public float state(GamepadState state) {
        return stateSupplier.apply(state);
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int centerY) {
        ResourceLocation texture = getTexture(gamepad.config().theme);
        graphics.blit(texture, x, centerY - 22 / 2, 0, 0, 22, 22, 22, 22);
    }

    @Override
    public DrawSize drawSize() {
        return new DrawSize(22, 22);
    }

    public String identifier() {
        return identifier;
    }

    @Override
    public Controller<GamepadState, ?> controller() {
        return this.gamepad;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", GamepadBinds.BIND_ID);
        object.addProperty("bind", identifier);
        return object;
    }

    private ResourceLocation getTexture(BuiltinGamepadTheme theme) {
        String themeId = theme.id();
        if (theme == BuiltinGamepadTheme.DEFAULT)
            themeId = gamepad.type().themeId();
        return new ResourceLocation("controlify", "textures/gui/gamepad/" + themeId + "/" + identifier + ".png");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamepadBind that = (GamepadBind) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
