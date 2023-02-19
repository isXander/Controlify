package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.BuiltinGamepadTheme;
import dev.isxander.controlify.controller.gamepad.GamepadConfig;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class GamepadBind implements IBind<GamepadState> {
    private final Function<GamepadState, Float> stateSupplier;
    private final String identifier;
    private final GamepadController gamepad;
    private final ResourceLocation defaultTexture;

    public GamepadBind(Function<GamepadState, Float> stateSupplier, String identifier, GamepadController gamepad) {
        this.stateSupplier = stateSupplier;
        this.identifier = identifier;
        this.gamepad = gamepad;
        this.defaultTexture = new ResourceLocation("controlify", "textures/gui/gamepad_buttons/" + gamepad.config().theme.id() + "/" + identifier + ".png");
    }

    @Override
    public float state(GamepadState state) {
        return stateSupplier.apply(state);
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY) {
        ResourceLocation texture = getTexture(gamepad.config().theme);

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(matrices, x, centerY - 22 / 2, 0, 0, 22, 22, 22, 22);
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
        if (theme == BuiltinGamepadTheme.DEFAULT)
            return defaultTexture;
        return new ResourceLocation("controlify", "textures/gui/gamepad_buttons/" + theme.id() + "/" + identifier + ".png");
    }
}
