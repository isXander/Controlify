package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Activator;
import net.minecraft.util.StringRepresentable;

public final class ActivatorSerialization {

	private ActivatorSerialization() {
	}

	public record Type<B extends Activator, T extends ActivatorFactory<B>>(String id, MapCodec<T> mapCodec) implements StringRepresentable {
		public static final Type<TapPulseActivator, TapPulseActivator.TapPulseActivatorConfig> TAP_PULSE =
				new Type<>(TapPulseActivator.TapPulseActivatorConfig.TYPE_ID, TapPulseActivator.TapPulseActivatorConfig.MAP_CODEC);

	}

	public static final Codec<ActivatorFactory<?>> FACTORY_CODEC = null;
}
