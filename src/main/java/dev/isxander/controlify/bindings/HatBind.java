package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class HatBind implements IBind {
    public static final String BIND_ID = "hat";

    private final ResourceLocation hat;
    private final HatState targetState;

    public HatBind(ResourceLocation hat, HatState targetState) {
        this.hat = hat;
        this.targetState = targetState;
    }

    @Override
    public float state(ControllerStateView state) {
        return state.getHatState(hat) == targetState ? 1 : 0;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int centerY, ControllerEntity controller) {
        ResourceLocation sprite = Inputs.getThemedSprite(hat, controller.info().type().namespace()).orElseThrow();

        /*? if >=1.20.3 {*/
        graphics.blitSprite(sprite, x, centerY - 11, 22, 22);
        /*?} else {*//*
        graphics.blit(sprite, x, centerY - 11, 0, 0, 22, 22, 22, 22);
        *//*?} */
    }

    @Override
    public DrawSize drawSize(ControllerEntity controller) {
        return new DrawSize(22, 22);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("hat", hat.toString());

        return object;
    }
}
