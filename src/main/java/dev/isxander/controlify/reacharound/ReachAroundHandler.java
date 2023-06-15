package dev.isxander.controlify.reacharound;

import dev.isxander.controlify.Controlify;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ReachAroundHandler {
    public static boolean reachAroundPolicy = true;

    public static HitResult getReachAroundHitResult(Entity entity, HitResult hitResult) {
       // if there is already a valid hit, we don't want to override it
        if (hitResult.getType() != HitResult.Type.MISS)
            return hitResult;

        if (!canReachAround(entity))
            return hitResult;

        // New method in 1.20 describing the position of the block
        // that the player is supported on.
        // This differentiates from the feet minus 1 as a block on the edge of the hitbox
        // may still support the player.
        var supportingBlockPos = entity.getOnPos();

        // player can be on ground but not directly over a block
        if (entity.level().getBlockState(supportingBlockPos).isAir())
            return hitResult;

        // this allows all interaction with blocks, such as opening containers, ringing bells, etc.
        // this is consistent with bedrock edition behaviour, tested
        return new BlockHitResult(supportingBlockPos.getCenter(), entity.getDirection(), supportingBlockPos, false);
    }

    private static boolean canReachAround(Entity cameraEntity) {
        return  reachAroundPolicy
                // don't want to place blocks while riding an entity
                && cameraEntity.getVehicle() == null
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
