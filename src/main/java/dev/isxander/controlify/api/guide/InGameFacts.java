package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.gui.guide.GuideDomains;
import dev.isxander.controlify.mixins.feature.guide.ingame.PlayerAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class InGameFacts {
    private InGameFacts() {}

    /** When {@link LocalPlayer#onGround()} is true */
    public static final Fact<InGameCtx> ON_GROUND = register(
            CUtil.rl("on_ground"),
            ctx -> ctx.player().onGround()
    );
    /** When {@link LocalPlayer#getVehicle()} is not null */
    public static final Fact<InGameCtx> IN_VEHICLE = register(
            CUtil.rl("in_vehicle"),
            ctx -> ctx.player().getVehicle() != null
    );
    /** When the currently ridden vehicle is a horse with a saddle */
    public static final Fact<InGameCtx> RIDING_SADDLED_HORSE = register(
            CUtil.rl("riding_saddled_horse"),
            ctx -> ctx.player().getVehicle() instanceof AbstractHorse horse && horse.isSaddled()
    );
    /** When the currently ridden vehicle is a Happy Ghast */
    public static final Fact<InGameCtx> RIDING_HAPPY_GHAST = register(
            CUtil.rl("riding_happy_ghast"),
            ctx -> ctx.player().getVehicle() instanceof HappyGhast
    );
    /** When the player is currently in creative flight */
    public static final Fact<InGameCtx> FLYING = register(
            CUtil.rl("flying"),
            ctx -> ctx.player().getAbilities().flying
    );
    /** When the player is currently gliding with an elytra */
    public static final Fact<InGameCtx> ELYTRA_FLYING = register(
            CUtil.rl("elytra_flying"),
            ctx -> ctx.player().isFallFlying()
    );
    /** When the player is in a state where pressing jump will cause the elytra to deploy */
    public static final Fact<InGameCtx> CAN_ELYTRA_FLY = register(
            CUtil.rl("can_elytra_fly"),
            ctx -> ((PlayerAccessor) ctx.player()).callCanGlide()
                    && !ctx.player().onClimbable()
                    && !ctx.player().onGround()
                    && !ctx.player().isInLiquid()
                    && !ctx.player().isFallFlying()
    );
    /** When the player is touching liquid, such as water or lava */
    public static final Fact<InGameCtx> IN_LIQUID = register(
            CUtil.rl("in_liquid"),
            ctx -> ctx.player().isInLiquid()
    );
    /** When the player is touching water */
    public static final Fact<InGameCtx> IN_WATER = register(
            CUtil.rl("in_water"),
            ctx -> ctx.player().isInWater()
    );
    /** When the player has their eyes underwater */
    public static final Fact<InGameCtx> UNDER_WATER = register(
            CUtil.rl("under_water"),
            ctx -> ctx.player().isUnderWater()
    );
    /** When the player is touching lava */
    public static final Fact<InGameCtx> IN_LAVA = register(
            CUtil.rl("in_lava"),
            ctx -> ctx.player().isInLava()
    );
    /** When the player is attempting to sneak (pressing the sneak key, or it is toggled on) */
    public static final Fact<InGameCtx> SNEAKING = register(
            CUtil.rl("sneaking"),
            ctx -> ctx.player().isShiftKeyDown()
    );
    /** When the player is using toggle sneak (does not mean it is currently toggled on) */
    public static final Fact<InGameCtx> IS_TOGGLE_SNEAK = register(
            CUtil.rl("is_toggle_sneak"),
            ctx -> ctx.controller().settings().generic.toggleSneak
    );
    /** When the player is using toggle sprint (does not mean it is currently toggled on) */
    public static final Fact<InGameCtx> IS_TOGGLE_SPRINT = register(
            CUtil.rl("is_toggle_sprint"),
            ctx -> ctx.controller().settings().generic.toggleSprint
    );
    /** When the player is attempting to sprint (pressing the sprint key, or it is toggled on) */
    public static final Fact<InGameCtx> SPRINTING = register(
            CUtil.rl("sprinting"),
            ctx -> ctx.player().input.keyPresses.sprint()
    );
    /** When the player is applying movement input - even if the player is not physically moving, if they're trying to, this fact goes */
    public static final Fact<InGameCtx> INPUT_MOVING = register(
            CUtil.rl("input_moving"),
            ctx -> !ctx.player().input.getMoveVector().equals(Vec2.ZERO)
    );
    /** When the player is in {@link GameType#SPECTATOR spectator mode} */
    public static final Fact<InGameCtx> IS_SPECTATOR = register(
            CUtil.rl("is_spectator"),
            ctx -> ctx.player().isSpectator()
    );
    /** When the player is in {@link GameType#CREATIVE creative mode} */
    public static final Fact<InGameCtx> IS_CREATIVE = register(
            CUtil.rl("is_creative"),
            ctx -> ctx.player().isCreative()
    );
    /** When the player is not invulnerable */
    public static final Fact<InGameCtx> HAS_HEARTS = register(
            CUtil.rl("has_hearts"),
            ctx -> !ctx.player().getAbilities().invulnerable
    );
    /** When the player is in {@link GameType#ADVENTURE adventure mode} */
    public static final Fact<InGameCtx> IS_ADVENTURE = register(
            CUtil.rl("is_adventure"),
            ctx -> ctx.player().gameMode() == GameType.ADVENTURE
    );
    /** When the player is in {@link GameType#SURVIVAL survival mode} */
    public static final Fact<InGameCtx> IS_SURVIVAL = register(
            CUtil.rl("is_survival"),
            ctx -> ctx.player().gameMode() == GameType.SURVIVAL
    );
    /** When the player is currently looking at an entity and is in range to interact with it */
    public static final Fact<InGameCtx> LOOKING_AT_ENTITY = register(
            CUtil.rl("looking_at_entity"),
            ctx -> ctx.hitResult().getType() == HitResult.Type.ENTITY
    );
    /** When the player is currently looking at a block and is in range to interact or destroy it */
    public static final Fact<InGameCtx> LOOKING_AT_BLOCK = register(
            CUtil.rl("looking_at_block"),
            ctx -> ctx.hitResult().getType() == HitResult.Type.BLOCK
    );
    /** When the player is neither {@link #LOOKING_AT_BLOCK looking at a block} or {@link #LOOKING_AT_ENTITY looking at an entity} */
    public static final Fact<InGameCtx> LOOKING_AT_AIR = register(
            CUtil.rl("looking_at_air"),
            ctx -> ctx.hitResult().getType() == HitResult.Type.MISS
    );
    /** When the player has an item in their main hand or their offhand */
    public static final Fact<InGameCtx> HAS_ITEM_IN_EITHER_HAND = register(
            CUtil.rl("has_item_in_either_hand"),
            ctx -> ctx.player().hasItemInSlot(EquipmentSlot.MAINHAND) || ctx.player().hasItemInSlot(EquipmentSlot.OFFHAND)
    );
    /** When the player has an item in their main hand. */
    public static final Fact<InGameCtx> HAS_ITEM_IN_MAINHAND = register(
            CUtil.rl("has_item_in_mainhand"),
            ctx -> ctx.player().hasItemInSlot(EquipmentSlot.MAINHAND)
    );
    /** When the player has an item in their offhand. */
    public static final Fact<InGameCtx> HAS_ITEM_IN_OFFHAND = register(
            CUtil.rl("has_item_in_offhand"),
            ctx -> ctx.player().hasItemInSlot(EquipmentSlot.OFFHAND)
    );
    /** When the player is holding an item stack with a count greater than one */
    public static final Fact<InGameCtx> HAS_MULTIPLE_ITEMS_IN_HAND = register(
            CUtil.rl("has_multiple_items_in_hand"),
            ctx -> {
                var holdingItem = ctx.player().getInventory().getSelectedItem();
                return !holdingItem.isEmpty() && holdingItem.getCount() > 1;
            }
    );

    private static Fact<InGameCtx> register(Identifier id, FactProvider<InGameCtx> provider) {
        var fact = Fact.of(id, provider);
        GuideDomains.IN_GAME.registerFact(fact);
        return fact;
    }
    private static Fact<InGameCtx> register(Identifier id) {
        return register(id, FactProvider.staticProvider(false));
    }

    public static void registerAll() {
        CommonFacts.registerAll();
        // This method is used to ensure that all facts are registered
        // when the class is loaded, so that they can be used in the guide.
        // No-op, as all facts are registered statically.
    }
}
