package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import dev.isxander.controlify.utils.predicates.ClientEntityPredicate;
//? if >=26.2 {
import dev.isxander.controlify.utils.predicates.ClientPredicateUtils;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
//?} else {
/*import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface ContextualPredicate {
	Codec<ContextualPredicate> CODEC = Codec.recursive("ContextualPredicate", self -> {
		Codec<Compound> compoundCodec = Compound.createCodec(self);

		return CExtraCodecs.fuzzy(
				List.of(Fact.CODEC, Item.CODEC, Block.CODEC, Entity.CODEC, compoundCodec, Static.CODEC),
				predicate -> switch (predicate) {
					case Fact _ -> Fact.CODEC;
					case Item _ -> Item.CODEC;
					case Block _ -> Block.CODEC;
					case Entity _ -> Entity.CODEC;
					case Compound _ -> compoundCodec;
					case Static _ -> Static.CODEC;
				}
		);
	});

	Set<Identifier> factDependencies();

	boolean matches(ContextualState state);

	record Compound(
			Optional<Set<ContextualPredicate>> allOf,
			Optional<Set<ContextualPredicate>> noneOf,
			Optional<Set<ContextualPredicate>> anyOf
	) implements ContextualPredicate {
		private static Codec<Compound> createCodec(Codec<ContextualPredicate> childCodec) {
			Codec<Compound> codec = RecordCodecBuilder.create(instance -> instance.group(
					CExtraCodecs.set(childCodec).optionalFieldOf("all_of").forGetter(Compound::allOf),
					CExtraCodecs.set(childCodec).optionalFieldOf("none_of").forGetter(Compound::noneOf),
					CExtraCodecs.set(childCodec).optionalFieldOf("any_of").forGetter(Compound::anyOf)
			).apply(instance, Compound::new));

			return codec.validate(compound -> {
				if (compound.allOf.isEmpty() && compound.noneOf.isEmpty() && compound.anyOf.isEmpty()) {
					return DataResult.error(() -> "Compound predicate must define at least one selector: 'all_of', 'none_of', 'any_of'");
				}

				return DataResult.success(compound);
			});
		}

		@Override
		public boolean matches(ContextualState state) {
			if (this.allOf.isPresent() && !this.allOf.get().stream().allMatch(predicate -> predicate.matches(state))) {
				return false;
			}

			if (this.noneOf.isPresent() && this.noneOf.get().stream().anyMatch(predicate -> predicate.matches(state))) {
				return false;
			}

			if (this.anyOf.isPresent() && this.anyOf.get().stream().noneMatch(predicate -> predicate.matches(state))) {
				return false;
			}

			return true;
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Stream.concat(Stream.concat(this.allOf.stream(), this.noneOf.stream()), this.anyOf.stream())
					.flatMap(Collection::stream)
					.flatMap(predicate -> predicate.factDependencies().stream())
					.collect(Collectors.toUnmodifiableSet());
		}
	}

	record Fact(Identifier fact) implements ContextualPredicate {
		private static final Codec<Fact> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("fact").forGetter(Fact::fact)
		).apply(instance, Fact::new));

		private static final Codec<Fact> COMPACT_CODEC = Identifier.CODEC.xmap(Fact::new, Fact::fact);

		private static final Codec<Fact> CODEC = FULL_CODEC.withAlternative(COMPACT_CODEC);

		@Override
		public boolean matches(ContextualState state) {
			return state.facts().getOrDefault(fact(), false);
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Set.of(fact());
		}
	}

	record Item(Identifier slot, ItemPredicate item) implements ContextualPredicate {
		private static final Codec<Item> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("slot").forGetter(Item::slot),
				ItemPredicate.CODEC.fieldOf("item").forGetter(Item::item)
		).apply(instance, Item::new));

		@Override
		public boolean matches(ContextualState state) {
			var itemInstance = state.items().get(slot());
			if (itemInstance == null) {
				return false;
			}
			return ClientPredicateUtils.matches(item(), itemInstance);
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Set.of();
		}
	}

	record Block(Identifier slot, BlockPredicate block) implements ContextualPredicate {
		private static final Codec<Block> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("slot").forGetter(Block::slot),
				BlockPredicate.CODEC.fieldOf("block").forGetter(Block::block)
		).apply(instance, Block::new));

		@Override
		public boolean matches(ContextualState state) {
			var blockInWorld = state.blocks().get(slot());
			if (blockInWorld == null) {
				return false;
			}
			return block().matches(blockInWorld);
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Set.of();
		}
	}

	record Entity(Identifier slot, ClientEntityPredicate entity) implements ContextualPredicate {
		private static final Codec<Entity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("slot").forGetter(Entity::slot),
				ClientEntityPredicate.CODEC.fieldOf("entity").forGetter(Entity::entity)
		).apply(instance, Entity::new));

		@Override
		public boolean matches(ContextualState state) {
			var entityInWorld = state.entities().get(slot());
			if (entityInWorld == null) {
				return false;
			}
			return entity().matches(Minecraft.getInstance().player, entityInWorld);
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Set.of();
		}
	}

	record Static(boolean value) implements ContextualPredicate {
		private static final Codec<Static> CODEC = Codec.BOOL.xmap(Static::new, Static::value);

		@Override
		public boolean matches(ContextualState state) {
			return value();
		}

		@Override
		public Set<Identifier> factDependencies() {
			return Set.of();
		}
	}
}
