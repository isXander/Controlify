package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.impl.action.KeyMappingHandle;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingHandle {
	@Shadow
	private int clickCount;

	@Shadow
	private boolean isDown;

	@Override
	public void controlify$click() {
		this.clickCount++;
	}

	@Override
	public void controlify$setDown(boolean down) {
		this.isDown = down;
	}
}
