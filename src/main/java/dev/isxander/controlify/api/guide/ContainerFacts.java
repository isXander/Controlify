package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.gui.guide.GuideDomains;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BundleItem;

public final class ContainerFacts {
    private ContainerFacts() {}

    /** When the user is hovering their cursor over a slot */
    public static final Fact<ContainerCtx> HOVERING_SLOT = register(
            CUtil.rl("hovering_slot"),
            ctx -> ctx.hoveredSlot() != null
    );
    /** When the user is hovering their cursor over an occupied slot */
    public static final Fact<ContainerCtx> HOVERING_ITEM = register(
            CUtil.rl("hovering_item"),
            ctx -> ctx.hoveredSlot() != null && ctx.hoveredSlot().hasItem()
    );
    /** When the user is hovering their cursor over an occupied slot which has more than one item in it */
    public static final Fact<ContainerCtx> HOVERING_MANY_ITEMS = register(
            CUtil.rl("hovering_many_items"),
            ctx -> ctx.hoveredSlot() != null && ctx.hoveredSlot().getItem().getCount() > 1
    );
    /** When the user has grabbed an item and is moving it around with their cursor */
    public static final Fact<ContainerCtx> HOLDING_ITEM = register(
            CUtil.rl("holding_item"),
            ctx -> !ctx.holdingItem().isEmpty()
    );
    /** When the user has grabbed an item and is moving it around with their cursor, and that item has more than one in the stack */
    public static final Fact<ContainerCtx> HOLDING_MANY_ITEMS = register(
            CUtil.rl("holding_many_items"),
            ctx -> ctx.holdingItem().getCount() > 1
    );
    /** When the container allows the player to place down their held item into the currently hovered slot */
    public static final Fact<ContainerCtx> CAN_PLACE_HELD_ITEM = register(
            CUtil.rl("can_place_held_item"),
            ctx -> ctx.hoveredSlot() != null && ctx.hoveredSlot().mayPlace(ctx.holdingItem())
    );
    /** When the user is hovering their cursor outside the container interface */
    public static final Fact<ContainerCtx> CURSOR_OUTSIDE_CONTAINER = register(
            CUtil.rl("cursor_outside_container"),
            ContainerCtx::cursorOutsideContainer
    );
    /** When the user is hovering their cursor over a slot which is occupied with an item tagged by BUNDLES */
    public static final Fact<ContainerCtx> HOVERING_ITEM_IS_BUNDLE = register(
            CUtil.rl("hovering_item_is_bundle")
            //? if >=1.21.2
            ,ctx -> ctx.hoveredSlot() != null && ctx.hoveredSlot().getItem().is(ItemTags.BUNDLES)
    );
    /** When the user is currently selecting an item from within the bundle they're hovering */
    public static final Fact<ContainerCtx> SELECTED_BUNDLE_SLOT = register(
            CUtil.rl("selected_bundle_slot")
            //? if >=1.21.2
            ,ctx -> ctx.hoveredSlot() != null && BundleItem.hasSelectedItem(ctx.hoveredSlot().getItem())
    );

    private static Fact<ContainerCtx> register(Identifier id, FactProvider<ContainerCtx> provider) {
        var fact = Fact.of(id, provider);

        GuideDomains.CONTAINER.registerFact(fact);

        return fact;
    }
    private static Fact<ContainerCtx> register(Identifier id) {
        return register(id, FactProvider.staticProvider(false));
    }

    public static void registerAll() {
        CommonFacts.registerAll();
        // This method is used to ensure that all facts are registered
        // when the class is loaded, so that they can be used in the guide.
        // No-op, as all facts are registered statically.
    }
}
