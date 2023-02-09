package dev.isxander.controlify.bindings;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.gui.ButtonRenderer;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class CompoundBind implements IBind {
    private final Set<Bind> binds;

    CompoundBind(Bind... binds) {
        this.binds = new LinkedHashSet<>(Arrays.asList(binds));
        if (this.binds.contains(Bind.NONE)) throw new IllegalArgumentException("Cannot have NONE in a compound bind!");
    }

    public Set<Bind> binds() {
        return ImmutableSet.copyOf(binds);
    }

    @Override
    public float state(ControllerState state, Controller controller) {
        return held(state, controller) ? 1f : 0f;
    }

    @Override
    public boolean held(ControllerState state, Controller controller) {
        return binds.stream().allMatch(bind -> bind.held(state, controller));
    }

    @Override
    public void draw(PoseStack matrices, int x, int centerY, Controller controller) {
        var font = Minecraft.getInstance().font;

        var iterator = binds.iterator();
        while (iterator.hasNext()) {
            var bind = iterator.next();

            bind.draw(matrices, x, centerY, controller);
            x += bind.drawSize().width();

            if (iterator.hasNext()) {
                font.drawShadow(matrices, "+", x + 1, centerY - font.lineHeight / 2f, 0xFFFFFF);
                x += font.width("+") + 2;
            }
        }
    }

    @Override
    public ButtonRenderer.DrawSize drawSize() {
        return new ButtonRenderer.DrawSize(
                binds.stream().map(IBind::drawSize).mapToInt(ButtonRenderer.DrawSize::width).sum() + (binds.size() - 1) * (2 + Minecraft.getInstance().font.width("+")),
                binds.stream().map(IBind::drawSize).mapToInt(ButtonRenderer.DrawSize::height).max().orElse(0)
        );
    }

    @Override
    public JsonElement toJson() {
        var list = new JsonArray();
        for (IBind bind : binds) {
            list.add(bind.toJson());
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompoundBind compoundBind && compoundBind.binds.equals(binds)
                || obj instanceof Bind bind && Set.of(bind).equals(binds);
    }
}
