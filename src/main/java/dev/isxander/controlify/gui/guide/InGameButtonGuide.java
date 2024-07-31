package dev.isxander.controlify.gui.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.guide.ActionPriority;
import dev.isxander.controlify.api.guide.GuideActionNameSupplier;
import dev.isxander.controlify.api.ingameguide.*;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.ColumnLayoutComponent;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;

import java.util.*;

public class InGameButtonGuide implements IngameGuideRegistry {
    private final ControllerEntity controller;
    private final LocalPlayer player;
    private final Minecraft minecraft = Minecraft.getInstance();

    private final List<GuideAction<IngameGuideContext>> leftGuides = new ArrayList<>();
    private final List<GuideAction<IngameGuideContext>> rightGuides = new ArrayList<>();

    private PositionedComponent<ColumnLayoutComponent<GuideActionRenderer<IngameGuideContext>>> leftLayout;
    private PositionedComponent<ColumnLayoutComponent<GuideActionRenderer<IngameGuideContext>>> rightLayout;

    public InGameButtonGuide(ControllerEntity controller, LocalPlayer localPlayer) {
        this.controller = controller;
        this.player = localPlayer;

        registerDefaultActions();
        ControlifyEvents.INGAME_GUIDE_REGISTRY.invoke(new ControlifyEvents.IngameGuideRegistryEvent(controller, this));

        Collections.sort(leftGuides);
        Collections.sort(rightGuides);

        refreshLayout();
    }

    public void refreshLayout() {
        boolean bottom = controller.genericConfig().config().ingameGuideBottom;

        leftLayout = new PositionedComponent<>(
                ColumnLayoutComponent.<GuideActionRenderer<IngameGuideContext>>builder()
                        .spacing(1)
                        .colPadding(2, 2)
                        .elementPosition(ColumnLayoutComponent.ElementPosition.LEFT)
                        .elements(leftGuides.stream().map(guide -> new GuideActionRenderer<>(guide, false, true)).toList())
                        .build(),
                !bottom ? AnchorPoint.TOP_LEFT : AnchorPoint.BOTTOM_LEFT,
                0, 0,
                !bottom ? AnchorPoint.TOP_LEFT : AnchorPoint.BOTTOM_LEFT
        );

        rightLayout = new PositionedComponent<>(
                ColumnLayoutComponent.<GuideActionRenderer<IngameGuideContext>>builder()
                        .spacing(1)
                        .colPadding(2, 2)
                        .elementPosition(ColumnLayoutComponent.ElementPosition.RIGHT)
                        .elements(rightGuides.stream().map(guide -> new GuideActionRenderer<>(guide, true, true)).toList())
                        .build(),
                !bottom ? AnchorPoint.TOP_RIGHT : AnchorPoint.BOTTOM_RIGHT,
                0, 0,
                !bottom ? AnchorPoint.TOP_RIGHT : AnchorPoint.BOTTOM_RIGHT
        );
    }

    public void renderHud(GuiGraphics graphics, float tickDelta) {
        boolean debugOpen = /*? if >=1.20.3 {*/
        minecraft.getDebugOverlay().showDebugScreen();
        /*?} else {*/
        /*minecraft.options.renderDebug;
        *//*?}*/
        boolean hideGui = minecraft.options.hideGui;

        if (!controller.genericConfig().config().showIngameGuide || minecraft.screen != null || debugOpen || hideGui)
            return;

        float scale = Controlify.instance().config().globalSettings().ingameButtonGuideScale;
        boolean customScale = scale != 1f;

        Matrix4f prevProjection = null;
        if (customScale) {
            prevProjection = RenderSystem.getProjectionMatrix();
            double guiScale = minecraft.getWindow().getGuiScale() * scale;
            Matrix4f matrix4f = new Matrix4f()
                    .setOrtho(
                            0.0F, (float)((double)minecraft.getWindow().getWidth() / guiScale), (float)((double)minecraft.getWindow().getHeight() / guiScale), 0.0F, 1000.0F, 21000.0F
                    );
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        }

        ControlifyCompat.ifBeginHudBatching();

        leftLayout.renderComponent(graphics, tickDelta);
        rightLayout.renderComponent(graphics, tickDelta);

        ControlifyCompat.ifEndHudBatching();

        if (customScale) {
            RenderSystem.setProjectionMatrix(prevProjection, VertexSorting.ORTHOGRAPHIC_Z);
        }
    }

    public void tick() {
        IngameGuideContext context = new IngameGuideContext(Minecraft.getInstance(), player, minecraft.level, calculateHitResult(), controller);

        leftLayout.getComponent().getChildComponents().forEach(renderer -> renderer.updateName(context));
        rightLayout.getComponent().getChildComponents().forEach(renderer -> renderer.updateName(context));

        double guiScale = minecraft.getWindow().getGuiScale() * Controlify.instance().config().globalSettings().ingameButtonGuideScale;
        int width = (int) (minecraft.getWindow().getWidth() / guiScale);
        int height = (int) (minecraft.getWindow().getHeight() / guiScale);

        leftLayout.updatePosition(width, height);
        rightLayout.updatePosition(width, height);
    }

    @Override
    public void registerGuideAction(InputBinding binding, ActionLocation location, GuideActionNameSupplier<IngameGuideContext> supplier) {
        this.registerGuideAction(binding, location, ActionPriority.NORMAL, supplier);
    }

    @Override
    public void registerGuideAction(InputBinding binding, ActionLocation location, ActionPriority priority, GuideActionNameSupplier<IngameGuideContext> supplier) {
        if (location == ActionLocation.LEFT)
            leftGuides.add(new GuideAction<>(binding, supplier, priority));
        else
            rightGuides.add(new GuideAction<>(binding, supplier, priority));
    }

    private void registerDefaultActions() {
        var options = Minecraft.getInstance().options;
        registerGuideAction(ControlifyBindings.JUMP.on(controller), ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (player.getAbilities().flying)
                return Optional.of(Component.translatable("controlify.guide.ingame.fly_up"));

            if (player.onGround())
                return Optional.of(Component.translatable("key.jump"));

            if (player.isInWater())
                return Optional.of(Component.translatable("controlify.guide.ingame.swim_up"));

            if (!player.onGround() && !player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION)) {
                var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chestStack))
                    return Optional.of(Component.translatable("controlify.guide.ingame.start_elytra"));
            }

            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.SNEAK.on(controller), ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (player.getVehicle() != null)
                return Optional.of(Component.translatable("controlify.guide.ingame.dismount"));
            if (player.getAbilities().flying)
                return Optional.of(Component.translatable("controlify.guide.ingame.fly_down"));
            if (player.isInWater() && !player.onGround())
                return Optional.of(Component.translatable("controlify.guide.ingame.swim_down"));
            if (ctx.controller().genericConfig().config().toggleSneak) {
                return Optional.of(Component.translatable(player.input.shiftKeyDown ? "controlify.guide.ingame.stop_sneaking" : "controlify.guide.ingame.start_sneaking"));
            } else {
                if (!player.input.shiftKeyDown)
                    return Optional.of(Component.translatable("controlify.guide.ingame.sneak"));
            }
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.SPRINT.on(controller), ActionLocation.LEFT, (ctx) -> {
            var player = ctx.player();
            if (!options.keySprint.isDown()) {
                if (!player.input.getMoveVector().equals(Vec2.ZERO)) {
                    if (player.isUnderWater())
                        return Optional.of(Component.translatable("controlify.guide.ingame.start_swimming"));
                    return Optional.of(Component.translatable("controlify.guide.ingame.start_sprinting"));
                }
            } else if (ctx.controller().genericConfig().config().toggleSprint) {
                if (player.isUnderWater())
                    return Optional.of(Component.translatable("controlify.guide.ingame.stop_swimming"));
                return Optional.of(Component.translatable("controlify.guide.ingame.stop_sprinting"));
            }
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.INVENTORY.on(controller), ActionLocation.RIGHT, (ctx) -> {
            if (ctx.client().screen == null)
                return Optional.of(Component.translatable("controlify.guide.ingame.inventory"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.RADIAL_MENU.on(controller), ActionLocation.RIGHT, ctx -> {
            if (ctx.client().screen == null)
                return Optional.of(Component.translatable("controlify.gui.radial_menu"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.ATTACK.on(controller), ActionLocation.RIGHT, (ctx) -> {
            var hitResult = ctx.hitResult();
            if (hitResult.getType() == HitResult.Type.ENTITY)
                if (player.isSpectator())
                    return Optional.of(Component.translatable("controlify.guide.ingame.spectate"));
                else
                    return Optional.of(Component.translatable("controlify.guide.ingame.attack"));
            if (hitResult.getType() == HitResult.Type.BLOCK)
                return Optional.of(Component.translatable("controlify.guide.ingame.break"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.USE.on(controller), ActionLocation.RIGHT, (ctx) -> {
            var hitResult = ctx.hitResult();
            var player = ctx.player();
            if (hitResult.getType() == HitResult.Type.ENTITY)
                if (player.isSpectator())
                    return Optional.of(Component.translatable("controlify.guide.ingame.spectate"));
                else
                    return Optional.of(Component.translatable("controlify.guide.ingame.interact"));
            if (hitResult.getType() == HitResult.Type.BLOCK || player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(Component.translatable("controlify.guide.ingame.use"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.DROP_INGAME.on(controller), ActionLocation.RIGHT, (ctx) -> {
            var holdingItem = ctx.player().getInventory().getSelected();
            if (!holdingItem.isEmpty())
                return Optional.of(Component.translatable("controlify.guide.ingame.drop"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.DROP_STACK.on(controller), ActionLocation.RIGHT, ctx -> {
            var holdingItem = ctx.player().getInventory().getSelected();
            if (!holdingItem.isEmpty() && holdingItem.getCount() > 1)
                return Optional.of(Component.translatable("controlify.binding.controlify.drop_stack"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.SWAP_HANDS.on(controller), ActionLocation.RIGHT, (ctx) -> {
            var player = ctx.player();
            if (player.hasItemInSlot(EquipmentSlot.MAINHAND) || player.hasItemInSlot(EquipmentSlot.OFFHAND))
                return Optional.of(Component.translatable("controlify.guide.ingame.swap_hands"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.PICK_BLOCK.on(controller), ActionLocation.RIGHT, (ctx) -> {
            if (ctx.hitResult().getType() == HitResult.Type.BLOCK && ctx.player().isCreative())
                return Optional.of(Component.translatable("controlify.guide.ingame.pick_block"));
            return Optional.empty();
        });
        registerGuideAction(ControlifyBindings.PICK_BLOCK_NBT.on(controller), ActionLocation.RIGHT, (ctx) -> {
            if (ctx.hitResult().getType() == HitResult.Type.BLOCK && ctx.player().isCreative())
                return Optional.of(Component.translatable("controlify.binding.controlify.pick_block_nbt"));
            return Optional.empty();
        });
    }

    private HitResult calculateHitResult() {
        /*? if >1.20.4 {*/
        double pickRange = minecraft.player.blockInteractionRange();
        /*?} else {*/
        /*double pickRange = minecraft.gameMode.getPickRange();
        *//*?}*/

        // block
        HitResult pickResult = player.pick(pickRange, 1f, false);

        Vec3 eyePos = player.getEyePosition(1f);

        /*? if >1.20.4 {*/
        pickRange = minecraft.player.entityInteractionRange();
        /*?} else {*/
        /*if (minecraft.gameMode.hasFarPickRange()) {
            pickRange = 6.0;
        }
        *//*?}*/

        double maxPickRange = pickResult.getLocation().distanceToSqr(eyePos);

        Vec3 viewVec = player.getViewVector(1f);
        Vec3 reachVec = eyePos.add(viewVec.x * pickRange, viewVec.y * pickRange, viewVec.z * pickRange);
        AABB box = player.getBoundingBox().expandTowards(viewVec.scale(pickRange)).inflate(1d, 1d, 1d);

        // entity
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
