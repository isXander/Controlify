package dev.isxander.controlify.ingame.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.ingameguide.*;
import dev.isxander.controlify.bindings.ControllerBindingImpl;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;

import java.util.*;

public class InGameButtonGuide implements IngameGuideRegistry {
    private final Controller<?, ?> controller;
    private final LocalPlayer player;
    private final Minecraft minecraft = Minecraft.getInstance();

    private final List<GuideActionSupplier> guidePredicates = new ArrayList<>();

    private final List<GuideAction> leftGuides = new ArrayList<>();
    private final List<GuideAction> rightGuides = new ArrayList<>();

    public InGameButtonGuide(Controller<?, ?> controller, LocalPlayer localPlayer) {
        this.controller = controller;
        this.player = localPlayer;

        registerDefaultActions();
        ControlifyEvents.INGAME_GUIDE_REGISTRY.invoker().onRegisterIngameGuide(controller.bindings(), this);
    }

    public void renderHud(PoseStack poseStack, float tickDelta, int width, int height) {
        if (!controller.config().showIngameGuide || minecraft.screen != null || minecraft.options.renderDebug)
            return;

        ControlifyCompat.ifBeginHudBatching();

        {
            var offset = 0;
            for (var action : leftGuides) {
                var renderer = action.binding().renderer();

                var drawSize = renderer.size();
                if (offset == 0) offset += drawSize.height() / 2;

                int x = 4;
                int y = 3 + offset;

                renderer.render(poseStack, x, y);

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
                var renderer = action.binding().renderer();

                var drawSize = renderer.size();
                if (offset == 0) offset += drawSize.height() / 2;

                int x = width - 4 - drawSize.width();
                int y = 3 + offset;

                renderer.render(poseStack, x, y);

                int textX = x - minecraft.font.width(action.name()) - 2;
                int textY = y - minecraft.font.lineHeight / 2;
                GuiComponent.fill(poseStack, textX - 1, textY - 1, textX + minecraft.font.width(action.name()) + 1, textY + minecraft.font.lineHeight + 1, 0x80000000);
                minecraft.font.draw(poseStack, action.name(), textX, textY, 0xFFFFFF);

                offset += drawSize.height() + 2;
            }
        }

        ControlifyCompat.ifEndHudBatching();
    }

    public void tick() {
        leftGuides.clear();
        rightGuides.clear();

        if (!controller.config().showIngameGuide || minecraft.screen != null)
            return;

        for (var actionPredicate : guidePredicates) {
            var action = actionPredicate.supply(Minecraft.getInstance(), player, minecraft.level, calculateHitResult(), controller);
            if (action.isEmpty())
                continue;

            GuideAction guideAction = action.get();
            if (!guideAction.binding().isUnbound()) {
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
    public void registerGuideAction(ControllerBinding binding, ActionLocation location, GuideActionNameSupplier supplier) {
        this.registerGuideAction(binding, location, ActionPriority.NORMAL, supplier);
    }

    @Override
    public void registerGuideAction(ControllerBinding binding, ActionLocation location, ActionPriority priority, GuideActionNameSupplier supplier) {
        guidePredicates.add(new GuideActionSupplier(binding, location, priority, supplier));
    }

    private void registerDefaultActions() {
        var options = Minecraft.getInstance().options;
        registerGuideAction(controller.bindings().JUMP, ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (player.getAbilities().flying)
                return Optional.of(Component.translatable("controlify.guide.fly_up"));

            if (player.isOnGround())
                return Optional.of(Component.translatable("key.jump"));

            if (player.isInWater())
                return Optional.of(Component.translatable("controlify.guide.swim_up"));

            if (!player.isOnGround() && !player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION)) {
                var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chestStack))
                    return Optional.of(Component.translatable("controlify.guide.start_elytra"));
            }

            return Optional.empty();
        });
        registerGuideAction(controller.bindings().SNEAK, ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (player.getVehicle() != null)
                return Optional.of(Component.translatable("controlify.guide.dismount"));
            if (player.getAbilities().flying)
                return Optional.of(Component.translatable("controlify.guide.fly_down"));
            if (player.isInWater() && !player.isOnGround())
                return Optional.of(Component.translatable("controlify.guide.swim_down"));
            if (ctx.controller().config().toggleSneak) {
                return Optional.of(Component.translatable(player.input.shiftKeyDown ? "controlify.guide.stop_sneaking" : "controlify.guide.start_sneaking"));
            } else {
                if (!player.input.shiftKeyDown)
                    return Optional.of(Component.translatable("controlify.guide.sneak"));
            }
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().SPRINT, ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (!options.keySprint.isDown()) {
                if (!player.input.getMoveVector().equals(Vec2.ZERO)) {
                    if (player.isUnderWater())
                        return Optional.of(Component.translatable("controlify.guide.start_swimming"));
                    return Optional.of(Component.translatable("controlify.guide.start_sprinting"));
                }
            } else if (ctx.controller().config().toggleSprint) {
                if (player.isUnderWater())
                    return Optional.of(Component.translatable("controlify.guide.stop_swimming"));
                return Optional.of(Component.translatable("controlify.guide.stop_sprinting"));
            }
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().INVENTORY, ActionLocation.RIGHT, (ctx) -> {
            if (ctx.client().screen == null)
                return Optional.of(Component.translatable("controlify.guide.inventory"));
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().ATTACK, ActionLocation.RIGHT, (ctx) -> {
            var hitResult = ctx.hitResult();
            if (hitResult.getType() == HitResult.Type.ENTITY)
                return Optional.of(Component.translatable("controlify.guide.attack"));
            if (hitResult.getType() == HitResult.Type.BLOCK)
                return Optional.of(Component.translatable("controlify.guide.break"));
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().USE, ActionLocation.RIGHT, (ctx) -> {
            var hitResult = ctx.hitResult();
            var player = ctx.player();
            if (hitResult.getType() == HitResult.Type.ENTITY)
                if (player.isSpectator())
                    return Optional.of(Component.translatable("controlify.guide.spectate"));
                else
                    return Optional.of(Component.translatable("controlify.guide.interact"));
            if (hitResult.getType() == HitResult.Type.BLOCK || player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(Component.translatable("controlify.guide.use"));
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().DROP, ActionLocation.RIGHT, (ctx) -> {
            var player = ctx.player();
            if (player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(Component.translatable("controlify.guide.drop"));
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().SWAP_HANDS, ActionLocation.RIGHT, (ctx) -> {
            var player = ctx.player();
            if (player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(Component.translatable("controlify.guide.swap_hands"));
            return Optional.empty();
        });
        registerGuideAction(controller.bindings().PICK_BLOCK, ActionLocation.RIGHT, (ctx) -> {
            if (ctx.hitResult().getType() == HitResult.Type.BLOCK && ctx.player().isCreative())
                return Optional.of(Component.translatable("controlify.guide.pick_block"));
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

    private record GuideActionSupplier(ControllerBinding binding, ActionLocation location, ActionPriority priority, GuideActionNameSupplier nameSupplier) {
        public Optional<GuideAction> supply(Minecraft client, LocalPlayer player, ClientLevel level, HitResult hitResult, Controller<?, ?> controller) {
            return nameSupplier.supply(new IngameGuideContext(client, player, level, hitResult, controller))
                    .map(name -> new GuideAction(binding, name, location, priority));
        }
    }
}
