/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.joml.Vector2f;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class TestUnitWorldContext implements ReversibleCallback {
	private static final int REGION_PADDING = 10;

	private final ClientGameTestContext context;
	private final TestSingleplayerContext world;
	private final ServerLevel level;
	private final int floorY;
	private int allocatedX;

	public TestUnitWorldContext(ClientGameTestContext context) {
		this.context = context;
		this.world = context.worldBuilder().setUseConsistentSettings(true).create();
		this.world.getClientLevel().waitForChunksDownload();
		this.level = this.world.getServer().computeOnServer(server -> CTestUtil.getPrincipalPlayer(server).level());
		this.floorY = this.world.getServer().computeOnServer(server -> {
			var player = CTestUtil.getPrincipalPlayer(server);
			return player.getBlockY() - 1;
		});
		this.allocatedX = 0;
	}

	public TestSingleplayerContext getWorld() {
		return this.world;
	}

	public TestRegionContext allocateRegion(int x, int y, int z) {
		return this.allocateRegion(new Vec3i(x, y, z));
	}

	public TestRegionContext allocateRegion(Vec3i size) {
		BlockPos origin = this.reserveRegion(size);
		var region = new TestRegionContext(origin, size);
		region.encaseRegion();
		context.waitTick();
		region.teleportPlayer(new BlockPos(1, 1, 1));
		context.waitTick();
		getWorld().getClientLevel().waitForChunksRender();
		return region;
	}

	/// @return region origin
	private BlockPos reserveRegion(Vec3i size) {
		this.allocatedX += size.getX() + REGION_PADDING;
		return new BlockPos(this.allocatedX, this.floorY + 1, 0);
	}

	@Override
	public void close() {
		this.world.close();
	}

	public class TestRegionContext implements ReversibleCallback {
		private final BlockPos origin;
		private final Vec3i size;
		private final AABB absoluteBounds;

		private final GameType playerPrevMode;
		private final Vec3 playerPrevPos;
		private final Vector2f playerPrevRot;

		public TestRegionContext(BlockPos origin, Vec3i size) {
			this.origin = origin;
			this.size = size;
			Preconditions.checkArgument(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, "Region size must be positive");
			this.absoluteBounds = AABB.encapsulatingFullBlocks(
					this.origin,
					this.origin.offset(size).offset(-1, -1, -1)
			);

			this.playerPrevMode = this.computeOnServer((_, player) -> player.gameMode());
			this.playerPrevPos = this.computeOnServer((_, player) -> player.position());
			this.playerPrevRot = this.computeOnServer((_, player) -> new Vector2f(player.getXRot(), player.getYRot()));
		}

		public TestSingleplayerContext getWorld() {
			return TestUnitWorldContext.this.getWorld();
		}

		public ServerLevel getLevel() {
			return TestUnitWorldContext.this.level;
		}

		public ServerPlayer getPlayer() {
			return this.getWorld().getServer().computeOnServer(CTestUtil::getPrincipalPlayer);
		}

		public AABB getAbsoluteBounds() {
			return this.absoluteBounds;
		}

		public BlockState getBlockState(BlockPos pos) {
			return this.computeOnServer((level, _) -> level.getBlockState(this.absolutePos(pos)));
		}

		public <E extends Entity> E spawn(EntityType<E> entityType, BlockPos pos) {
			return this.computeOnServer((level, _) -> {
				E entity = entityType.spawn(level, this.absolutePos(pos), EntitySpawnReason.STRUCTURE);
				if (entity instanceof Mob mob) {
					mob.setPersistenceRequired();
				}
				return entity;
			});
		}

		public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, BlockPos pos) {
			E mob = this.spawn(entityType, pos);
			this.runOnServer((_, _) -> mob.removeFreeWill());
			return mob;
		}

		public void killAllEntities() {
			this.killAllEntitiesOfClass(Entity.class);
		}

		public void killAllEntitiesOfClass(Class<? extends Entity> baseClass) {
			this.runOnServer((level, _) -> {
				AABB bounds = this.getAbsoluteBounds();
				List<? extends Entity> entities = level.getEntitiesOfClass(baseClass, bounds.inflate(1.0), mob -> !(mob instanceof Player));
				entities.forEach(entity -> entity.kill(level));
			});
		}

		public void setBlock(int x, int y, int z, Block block) {
			this.setBlock(new BlockPos(x, y, z), block);
		}

		public void setBlock(int x, int y, int z, BlockState state) {
			this.setBlock(new BlockPos(x, y, z), state);
		}

		public void setBlock(BlockPos blockPos, Block block) {
			this.setBlock(blockPos, block.defaultBlockState());
		}

		public void setBlock(BlockPos blockPos, BlockState state) {
			this.runOnServer((level, _) -> {
				level.setBlock(this.absolutePos(blockPos), state, 3);
			});
		}

		public void hurt(Entity target, DamageSource source, float damage) {
			this.runOnServer((level, _) -> target.hurtServer(level, source, damage));
		}

		public void attack(LivingEntity source, Entity target) {
			this.runOnServer((level, _) -> source.doHurtTarget(level, target));
		}

		public void kill(Entity entity) {
			this.runOnServer((level, _) -> entity.kill(level));
		}

		public void discard(Entity entity) {
			this.runOnServer((_, _) -> entity.discard());
		}

		public void teleport(Entity entity, BlockPos pos) {
			this.runOnServer((level, _) -> {
				Vec3 destination = Vec3.atBottomCenterOf(this.absolutePos(pos));
				boolean teleported = entity.teleportTo(
						level,
						destination.x,
						destination.y,
						destination.z,
						Set.of(),
						entity.getYRot(),
						entity.getXRot(),
						true
				);
				Preconditions.checkState(teleported, "Failed to teleport entity into test region");

				// Match TeleportCommand: do not carry falling velocity into the test and
				// let the next movement tick establish the entity's grounded state.
				entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
				entity.setOnGround(true);
			});
		}

		public void teleportPlayer(BlockPos pos) {
			this.teleport(this.getPlayer(), pos);
		}

		public BlockPos getEntityBlockPos(Entity entity) {
			return this.computeOnServer((_, _) -> this.relativePos(entity.blockPosition()));
		}

		public BlockPos getPlayerBlockPos() {
			return this.getEntityBlockPos(this.getPlayer());
		}

		public BlockPos absolutePos(BlockPos relativePos) {
			Preconditions.checkArgument(relativePos.getX() >= 0 && relativePos.getX() < size.getX());
			Preconditions.checkArgument(relativePos.getY() >= 0 && relativePos.getY() < size.getY());
			Preconditions.checkArgument(relativePos.getZ() >= 0 && relativePos.getZ() < size.getZ());
			return origin.offset(relativePos);
		}

		public BlockPos relativePos(BlockPos absolutePos) {
			return absolutePos.subtract(this.origin);
		}

		private void encaseRegion() {
			this.runOnServer((level, _) -> {
				this.processRegionBoundary(blockPos -> {
					level.setBlockAndUpdate(blockPos, Blocks.BARRIER.defaultBlockState());
				});
			});
		}

		/// in absolute coordinate space
		private void processRegionBoundary(Consumer<BlockPos> action) {
			AABB bounds = this.getAbsoluteBounds();
			boolean hasCeiling = false;
			BlockPos low = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ).offset(-1, -1, -1);
			BlockPos high = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
			BlockPos.betweenClosedStream(low, high)
				.forEach(
					blockPos -> {
						boolean isNonCeilingEdge = blockPos.getX() == low.getX()
							|| blockPos.getX() == high.getX()
							|| blockPos.getZ() == low.getZ()
							|| blockPos.getZ() == high.getZ()
							|| blockPos.getY() == low.getY();
						boolean isCeiling = blockPos.getY() == high.getY();
						if (isNonCeilingEdge || isCeiling && hasCeiling) {
							action.accept(blockPos);
						}
					}
				);
		}

		public <T, E extends Throwable> T computeOnServer(FailableBiFunction<ServerLevel, ServerPlayer, T, E> action) throws E {
			return this.getWorld().getServer().computeOnServer(server -> {
				var player = CTestUtil.getPrincipalPlayer(server);
				var level = player.level();
				return action.apply(level, player);
			});
		}

		public <E extends Throwable> void runOnServer(FailableBiConsumer<ServerLevel, ServerPlayer, E> action) throws E {
			this.getWorld().getServer().runOnServer(server -> {
				var player = CTestUtil.getPrincipalPlayer(server);
				var level = player.level();
				action.accept(level, player);
			});
		}

		@Override
		public void close() {
			this.killAllEntities();
			this.runOnServer((level, player) -> {
				player.setGameMode(this.playerPrevMode);
				boolean teleported = player.teleportTo(
						level,
						this.playerPrevPos.x,
						this.playerPrevPos.y,
						this.playerPrevPos.z,
						Set.of(),
						this.playerPrevRot.y,
						this.playerPrevRot.x,
						true
				);
				Preconditions.checkState(teleported, "Failed to restore player position after test region");
				player.setHealth(player.getMaxHealth());
				player.clearFire();
				player.clearRaidOmenPosition();
				player.clearFreeze();
				player.removeAllEffects();
				player.removeVehicle();
				player.getInventory().clearContent();
				player.getInventory().setChanged();
				player.containerMenu.broadcastChanges();
			});
			context.waitTicks(2);
		}
	}
}
