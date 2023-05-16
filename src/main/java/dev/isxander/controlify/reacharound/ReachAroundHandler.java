package dev.isxander.controlify.reacharound;

import dev.isxander.controlify.Controlify;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ReachAroundHandler {
    public static HitResult getReachAroundHitResult(Entity entity, HitResult hitResult) {
        // if there is already a valid hit, we don't want to override it
        if (hitResult.getType() != HitResult.Type.MISS)
            return hitResult;

        if (!canReachAround(entity))
            return hitResult;

        // LivingEntity#playBlockFallSound - this is the location where the game determines the footstep noise
        // maybe experiment on different values rather than 0.2f from other areas in the game?
        int x = Mth.floor(entity.getX());
        int y = Mth.floor(entity.getY() - 0.2F);
        int z = Mth.floor(entity.getZ());
        var floorPos = new BlockPos(x, y, z);

        // this allows all interaction with blocks, such as opening containers, ringing bells, etc.
        // this is consistent with bedrock edition behaviour, tested
        return new BlockHitResult(floorPos.getCenter(), entity.getDirection(), floorPos, false);
    }

    private static boolean canReachAround(Entity cameraEntity) {
        return  // don't want to place blocks while riding an entity
                cameraEntity.getVehicle() == null
                // straight ahead = 0deg, up = -90deg, down = 90deg
                // 45deg and above is half between straight ahead and down, must be lower or equal to this threshold
                && cameraEntity.getXRot() >= 45
                // if the player is not standing on a block, this is inappropriate
                // this also prevents selecting fluids as a valid position
                && cameraEntity.onGround()
                // must respect config option
                && Controlify.instance().config().globalSettings().reachAround.canReachAround();
    }
}
