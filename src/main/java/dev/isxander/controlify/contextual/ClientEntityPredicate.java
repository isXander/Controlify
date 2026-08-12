package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.utils.ClientPredicateUtils;
import net.minecraft.advancements.predicates.DistancePredicate;
import net.minecraft.advancements.predicates.MobEffectsPredicate;
import net.minecraft.advancements.predicates.SlotsPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityFlagsPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.predicates.entity.MovementPredicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/// A subset of {@link net.minecraft.advancements.predicates.entity.EntityPredicate} that works
/// for all entities on the client. The vanilla one requires server-side-only data.
///
/// This predicate also queries client-sided tags via {@link ClientPredicateUtils}.
///
/// This predicate is missing the following from its server-sided counterpart:
/// - `location`
/// - `nbt`
/// - `type_specific`
/// - `targeted_entity`
/// - data component matcher
public record ClientEntityPredicate(
		Optional<EntityTypePredicate> entityType,
		Optional<DistancePredicate> distanceToPlayer,
		Optional<MovementPredicate> movement,
		Optional<MobEffectsPredicate> effects,
		Optional<EntityFlagsPredicate> flags,
		Optional<EntityEquipmentPredicate> equipment,
		Optional<Integer> periodicTick,
		Optional<ClientEntityPredicate> vehicle,
		Optional<ClientEntityPredicate> passenger,
		Optional<String> team,
		Optional<SlotsPredicate> slots
) {
	public static final Codec<ClientEntityPredicate> CODEC = Codec.recursive(
			"ClientEntityPredicate",
			subCodec -> RecordCodecBuilder.create(instance -> instance.group(
					EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(ClientEntityPredicate::entityType),
					DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(ClientEntityPredicate::distanceToPlayer),
					MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(ClientEntityPredicate::movement),
					MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(ClientEntityPredicate::effects),
					EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(ClientEntityPredicate::flags),
					EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(ClientEntityPredicate::equipment),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(ClientEntityPredicate::periodicTick),
					subCodec.optionalFieldOf("vehicle").forGetter(ClientEntityPredicate::vehicle),
					subCodec.optionalFieldOf("passenger").forGetter(ClientEntityPredicate::passenger),
					Codec.STRING.optionalFieldOf("team").forGetter(ClientEntityPredicate::team),
					SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(ClientEntityPredicate::slots)
			).apply(instance, ClientEntityPredicate::new))
	);

	public boolean matches(LocalPlayer player, @Nullable Entity entity) {
		return this.matches(player.position(), entity);
	}

	public boolean matches(@Nullable Vec3 position, Entity entity) {
		if (entity == null) {
			return false;
		}

		if (this.entityType.isPresent() && !ClientPredicateUtils.matches(this.entityType.get(), entity)) {
			return false;
		}

		if (position == null) {
			if (this.distanceToPlayer.isPresent()) {
				return false;
			}
		} else if (this.distanceToPlayer.isPresent()
				&& this.distanceToPlayer.get().matches(position.x, position.y, position.z, entity.getX(), entity.getY(), entity.getZ())) {
			return false;
		}

		if (this.movement.isPresent()) {
			Vec3 knownMovement = entity.getKnownMovement();
			Vec3 velocity = knownMovement.scale(20.0);
			if (!this.movement.get().matches(velocity.x, velocity.y, velocity.z, entity.fallDistance)) {
				return false;
			}
		}

		if (this.effects.isPresent() && !effects.get().matches(entity)) {
			return false;
		}

		if (this.flags.isPresent() && !this.flags.get().matches(entity)) {
			return false;
		}

		if (this.equipment.isPresent() && !this.equipment.get().matches(entity)) {
			return false;
		}

		if (this.vehicle.isPresent() && !this.vehicle.get().matches(position, entity.getVehicle())) {
			return false;
		}

		if (this.passenger.isPresent() && entity.getPassengers().stream().noneMatch(p -> this.passenger.get().matches(position, p))) {
			return false;
		}

		if (this.periodicTick.isPresent() && entity.tickCount % this.periodicTick.get() != 0) {
			return false;
		}

		if (this.team.isPresent()) {
			Team team = entity.getTeam();
			if (team == null || !this.team.get().equals(team.getName())) {
				return false;
			}
		}

		if (this.slots.isPresent() && !ClientPredicateUtils.matches(this.slots.get(), entity)) {
			return false;
		}

		return true;
	}
}
