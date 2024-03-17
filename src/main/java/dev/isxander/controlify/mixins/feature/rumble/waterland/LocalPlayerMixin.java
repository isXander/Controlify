package dev.isxander.controlify.mixins.feature.rumble.waterland;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends PlayerMixin {
    protected LocalPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void splashRumble(CallbackInfo ci) {
        ControlifyApi.get().getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble -> {
            Entity entity = Objects.requireNonNullElse(this.getControllingPassenger(), this);
            float f = entity == this ? 0.2F : 0.9F;
            Vec3 vec3 = entity.getDeltaMovement();
            float impactForce = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * f);

            if (impactForce >= 0.05f) {
                float multiplier = Math.min(1, impactForce / 0.5f);
                rumble.rumbleManager().play(
                        RumbleSource.PLAYER,
                        BasicRumbleEffect.byTime(
                                t -> new RumbleState(multiplier * (1 - t), multiplier * 0.5f),
                                impactForce < 0.25f ? 10 : 20
                        )
                );
            }
        });
    }
}
