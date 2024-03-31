package dev.isxander.controlify.bindings;

import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class AxisBind implements IBind {
    public static final String BIND_ID = "axis";

    private final ResourceLocation axis;

    public AxisBind(ResourceLocation axis) {
        this.axis = axis;
    }

    @Override
    public float state(ControllerStateView state) {
        return state.getAxisState(axis);
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int centerY, ControllerEntity controller) {
        Optional<ResourceLocation> spriteOpt = Inputs.getThemedSprite(axis, controller.info().type().namespace());

        /*? if >=1.20.3 {*/
        ResourceLocation sprite = spriteOpt.orElse(Controlify.id("inputs/unknown/axis/blank"));

        graphics.blitSprite(sprite, x, centerY - 11, 22, 22);
        /*?} else {*//*
        ResourceLocation sprite = spriteOpt.orElse(Controlify.id("textures/gui/sprites/inputs/unknown/axis/blank.png"));

        graphics.blit(sprite, x, centerY - 11, 0, 0, 22, 22, 22, 22);
        *//*?} */


        // if unknown, draw string over top
        if (spriteOpt.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            String[] parts = axis.getPath().split("/");

            graphics.drawCenteredString(
                    font,
                    parts[parts.length - 1],
                    x + 11,
                    centerY - font.lineHeight / 2,
                    -1
            );
        }
    }

    @Override
    public DrawSize drawSize(ControllerEntity controller) {
        int width = 22;

        Optional<ResourceLocation> spriteOpt = Inputs.getThemedSprite(axis, controller.info().type().namespace());
        if (spriteOpt.isEmpty()) {
            String[] parts = axis.getPath().split("/");

            int textWidth = Minecraft.getInstance().font.width(parts[parts.length - 1]);
            width = Math.max(textWidth, width);
        }

        return new DrawSize(width, 22);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", BIND_ID);
        object.addProperty("axis", axis.toString());

        return object;
    }
}
