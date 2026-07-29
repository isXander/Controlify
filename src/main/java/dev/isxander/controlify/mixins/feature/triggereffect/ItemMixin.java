package dev.isxander.controlify.mixins.feature.triggereffect;

import dev.isxander.controlify.controller.dualsense.TriggerEffectHolder;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin implements TriggerEffectHolder {
	@Unique
	private @Nullable DualsenseTriggerEffect controlify$useEffect = null;

	@Unique
	private @Nullable DualsenseTriggerEffect controlify$swingEffect = null;

	@Override
	public Optional<DualsenseTriggerEffect> controlify$getUseTriggerEffect() {
		return Optional.ofNullable(controlify$useEffect);
	}

	@Override
	public Optional<DualsenseTriggerEffect> controlify$getSwingTriggerEffect() {
		return Optional.ofNullable(controlify$swingEffect);
	}

	@Override
	public void controlify$assignUseTriggerEffect(@NotNull DualsenseTriggerEffect effect) {
		this.controlify$useEffect = effect;
	}

	@Override
	public void controlify$assignSwingTriggerEffect(@NotNull DualsenseTriggerEffect effect) {
		this.controlify$swingEffect = effect;
	}
}
