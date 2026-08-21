package dev.isxander.controlify.utils.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

//? if >=26.2 {
import net.minecraft.advancements.predicates.ItemPredicate;
//?} else {
/*import net.minecraft.advancements.criterion.ItemPredicate;
*///?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public record EntityEquipmentPredicate(
		Optional<ItemPredicate> head,
		Optional<ItemPredicate> chest,
		Optional<ItemPredicate> legs,
		Optional<ItemPredicate> feet,
		Optional<ItemPredicate> body,
		Optional<ItemPredicate> mainhand,
		Optional<ItemPredicate> offhand,
		Optional<ItemPredicate> saddle
) {
	public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
			i -> i.group(
							ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head),
							ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest),
							ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs),
							ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet),
							ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body),
							ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand),
							ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand),
							ItemPredicate.CODEC.optionalFieldOf("saddle").forGetter(EntityEquipmentPredicate::saddle)
					)
					.apply(i, EntityEquipmentPredicate::new)
	);

	public boolean matches(final @Nullable Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			if (this.head.isPresent() && !ClientPredicateUtils.matches(this.head.get(), livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
				return false;
			} else if (this.chest.isPresent() && !ClientPredicateUtils.matches(this.chest.get(), livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
				return false;
			} else if (this.legs.isPresent() && !ClientPredicateUtils.matches(this.legs.get(), livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
				return false;
			} else if (this.feet.isPresent() && !ClientPredicateUtils.matches(this.feet.get(), livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
				return false;
			} else if (this.body.isPresent() && !ClientPredicateUtils.matches(this.body.get(), livingEntity.getItemBySlot(EquipmentSlot.BODY))) {
				return false;
			} else if (this.mainhand.isPresent() && !ClientPredicateUtils.matches(this.mainhand.get(), livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
				return false;
			} else if (this.offhand.isPresent() && !ClientPredicateUtils.matches(this.offhand.get(), livingEntity.getItemBySlot(EquipmentSlot.OFFHAND))) {
				return false;
			} else if (this.saddle.isPresent() && !ClientPredicateUtils.matches(this.saddle.get(), livingEntity.getItemBySlot(EquipmentSlot.SADDLE))) {
				return false;
			}

			return true;
		}

		return false;
	}
}
