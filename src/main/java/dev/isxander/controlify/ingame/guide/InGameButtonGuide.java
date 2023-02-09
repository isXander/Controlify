package dev.isxander.controlify.ingame.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;

import java.util.*;

public class InGameButtonGuide implements ButtonGuideRegistry {
    private final Controller controller;
    private final LocalPlayer player;
    private final Minecraft minecraft = Minecraft.getInstance();

    private final List<ButtonActionSupplier> guidePredicates = new ArrayList<>();

    private final List<GuideAction> leftGuides = new ArrayList<>();
    private final List<GuideAction> rightGuides = new ArrayList<>();

    public InGameButtonGuide(Controller controller, LocalPlayer localPlayer) {
        this.controller = controller;
        this.player = localPlayer;

        registerDefaultActions();
        ControlifyEvents.BUTTON_GUIDE_REGISTRY.invoker().onRegisterButtonGuide(this);
    }

    public void renderHud(PoseStack poseStack, float tickDelta, int width, int height) {
        if (!controller.config().showGuide || minecraft.screen != null)
            return;

        {
            var offset = 0;
            for (var action : leftGuides) {
                var bind = action.binding().currentBind();

                var drawSize = bind.drawSize();
                if (offset == 0) offset += drawSize.height() / 2;

                int x = 4;
                int y = 3 + offset;

                bind.draw(poseStack, x, y, controller);

                int textX = x + drawSize.width() + 2;
                int textY = y - minecraft.font.lineHeight / 2;
                GuiComponent.fill(poseStack, textX - 1, textY - 1, textX + minecraft.font.width(action.name()) + 1, textY + minecraft.font.lineHeight + 1, 0x80000000);
                minecraft.font.draw(poseStack, action.name(), textX, textY, 0xFFFFFF);

                offset += drawSize.height() + 2;
            }
        }

        {
            var offset = 0;
            for (var action : rightGuides) {
                var bind = action.binding().currentBind();

                var drawSize = bind.drawSize();
                if (offset == 0) offset += drawSize.height() / 2;

                int x = width - 4 - drawSize.width();
                int y = 3 + offset;

                bind.draw(poseStack, x, y, controller);

                int textX = x - minecraft.font.width(action.name()) - 2;
                int textY = y - minecraft.font.lineHeight / 2;
                GuiComponent.fill(poseStack, textX - 1, textY - 1, textX + minecraft.font.width(action.name()) + 1, textY + minecraft.font.lineHeight + 1, 0x80000000);
                minecraft.font.draw(poseStack, action.name(), textX, textY, 0xFFFFFF);

                offset += drawSize.height() + 2;
            }
        }
    }

    public void tick() {
        leftGuides.clear();
        rightGuides.clear();

        if (!controller.config().showGuide)
            return;

        for (var actionPredicate : guidePredicates) {
            var action = actionPredicate.supply(Minecraft.getInstance(), player, minecraft.level, calculateHitResult(), controller);
            if (action.isPresent()) {
                if (action.get().location() == ActionLocation.LEFT)
                    leftGuides.add(action.get());
                else
                    rightGuides.add(action.get());
            }
        }

        Collections.sort(leftGuides);
        Collections.sort(rightGuides);
    }

    @Override
    public void registerGuideAction(ButtonActionSupplier supplier) {
        guidePredicates.add(supplier);
    }

    private void registerDefaultActions() {
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (player.getAbilities().flying)
                return Optional.of(new GuideAction(controller.bindings().JUMP, Component.translatable("controlify.guide.fly_up"), ActionLocation.LEFT));

            if (player.isOnGround())
                return Optional.of(new GuideAction(controller.bindings().JUMP, Component.translatable("key.jump"), ActionLocation.LEFT));

            if (player.isInWater())
                return Optional.of(new GuideAction(controller.bindings().JUMP, Component.translatable("controlify.guide.swim_up"), ActionLocation.LEFT));

            if (!player.isOnGround() && !player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION)) {
                var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chestStack))
                    return Optional.of(new GuideAction(controller.bindings().JUMP, Component.translatable("controlify.guide.start_elytra"), ActionLocation.LEFT));
            }

            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (player.getVehicle() != null)
                return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.dismount"), ActionLocation.LEFT));
            if (player.getAbilities().flying)
                return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.fly_down"), ActionLocation.LEFT));
            if (player.isInWater())
                return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.swim_down"), ActionLocation.LEFT));
            if (controller.config().toggleSneak) {
                if (player.input.shiftKeyDown)
                    return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.stop_sneaking"), ActionLocation.LEFT));
                else
                    return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.start_sneaking"), ActionLocation.LEFT));
            } else {
                if (!player.input.shiftKeyDown)
                    return Optional.of(new GuideAction(controller.bindings().SNEAK, Component.translatable("controlify.guide.sneak"), ActionLocation.LEFT));
            }
            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (client.screen == null)
                return Optional.of(new GuideAction(controller.bindings().INVENTORY, Component.translatable("controlify.guide.inventory"), ActionLocation.RIGHT));
            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (hitResult.getType() == HitResult.Type.ENTITY)
                return Optional.of(new GuideAction(controller.bindings().ATTACK, Component.translatable("controlify.guide.attack"), ActionLocation.RIGHT));
            if (hitResult.getType() == HitResult.Type.BLOCK)
                return Optional.of(new GuideAction(controller.bindings().ATTACK, Component.translatable("controlify.guide.break"), ActionLocation.RIGHT));
            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (hitResult.getType() == HitResult.Type.ENTITY)
                return Optional.of(new GuideAction(controller.bindings().USE, Component.translatable("controlify.guide.interact"), ActionLocation.RIGHT));
            if (hitResult.getType() == HitResult.Type.BLOCK || player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(new GuideAction(controller.bindings().USE, Component.translatable("controlify.guide.use"), ActionLocation.RIGHT));
            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(new GuideAction(controller.bindings().DROP, Component.translatable("controlify.guide.drop"), ActionLocation.RIGHT));
            return Optional.empty();
        });
        registerGuideAction((client, player, level, hitResult, controller) -> {
            if (hitResult.getType() == HitResult.Type.BLOCK && player.isCreative())
                return Optional.of(new GuideAction(controller.bindings().PICK_BLOCK, Component.translatable("controlify.guide.pick_block"), ActionLocation.RIGHT));
            return Optional.empty();
        });
    }

    private HitResult calculateHitResult() {
        double pickRange = minecraft.gameMode.getPickRange();
        HitResult pickResult = player.pick(pickRange, 1f, false);

        Vec3 eyePos = player.getEyePosition(1f);

        if (minecraft.gameMode.hasFarPickRange()) {
            pickRange = 6.0;
        }
        double maxPickRange = pickResult.getLocation().distanceToSqr(eyePos);

        Vec3 viewVec = player.getViewVector(1f);
        Vec3 reachVec = eyePos.add(viewVec.x * pickRange, viewVec.y * pickRange, viewVec.z * pickRange);
        AABB box = player.getBoundingBox().expandTowards(viewVec.scale(pickRange)).inflate(1d, 1d, 1d);

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                player, eyePos, reachVec, box, (entity) -> !entity.isSpectator() && entity.isPickable(), maxPickRange
        );

        if (entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePos) < pickResult.getLocation().distanceToSqr(eyePos)) {
            return entityHitResult;
        } else {
            return pickResult;
        }
    }

}
