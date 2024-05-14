package dev.isxander.controlify.bindings.v2.defaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public record LayeredDefaultBindProvider(List<Layer> layers) implements DefaultBindProvider {
    public static final Codec<LayeredDefaultBindProvider> CODEC = Codec.list(Layer.CODEC, 1, Integer.MAX_VALUE)
            .xmap(LayeredDefaultBindProvider::new, LayeredDefaultBindProvider::layers);

    public static DefaultBindProvider of(Layer... layers) {
        return new LayeredDefaultBindProvider(Arrays.asList(layers));
    }

    @Override
    @Nullable
    public Bind getDefaultBind(ResourceLocation bindId) {
        for (var layer : layers()) {
            Bind bind = layer.provider().getDefaultBind(bindId);
            if (bind != null) return bind;
            if (layer.clearBelow()) return null;
        }
        return null;
    }

    public record Layer(DefaultBindProvider provider, boolean clearBelow) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        MapCodec.assumeMapUnsafe(MapBackedDefaultBindProvider.CODEC).forGetter(layer -> (MapBackedDefaultBindProvider) layer.provider()),
                        Codec.BOOL.fieldOf("clear_below").forGetter(Layer::clearBelow)
                ).apply(instance, Layer::new));
    }
}
