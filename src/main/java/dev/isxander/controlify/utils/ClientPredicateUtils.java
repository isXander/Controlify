package dev.isxander.controlify.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.SlotsPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ClientPredicateUtils {

	private ClientPredicateUtils() {
	}

	public static boolean matches(EntityTypePredicate predicate, Entity entity) {
		return CUtil.isInWithLocalFallback(entity, predicate.types());
	}

	public static boolean matches(ItemPredicate predicate, ItemStack stack) {
		if (predicate.items().isPresent() && !CUtil.isInWithLocalFallback(stack, predicate.items().get())) {
			return false;
		} else {
			return predicate.count().matches(stack.count()) && predicate.components().test(stack);
		}
	}

	public static boolean matches(SlotsPredicate predicate, SlotProvider slotProvider) {
		for (Map.Entry<SlotRange, ItemPredicate> entry : predicate.slots().entrySet()) {
			if (!matchSlots(slotProvider, entry.getValue(), entry.getKey().slots())) {
				return false;
			}
		}

		return true;
	}

	private static boolean matchSlots(SlotProvider slotProvider, ItemPredicate test, IntList slots) {
		for (int i = 0; i < slots.size(); i++) {
			int slotId = slots.getInt(i);
			SlotAccess slot = slotProvider.getSlot(slotId);
			if (slot != null && ClientPredicateUtils.matches(test, slot.get())) {
				return true;
			}
		}

		return false;
	}
}
