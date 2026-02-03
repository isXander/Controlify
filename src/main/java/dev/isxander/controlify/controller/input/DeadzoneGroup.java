package dev.isxander.controlify.controller.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record DeadzoneGroup(
        Identifier name,
        List<Identifier> axes
) {
    public static final Codec<DeadzoneGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("name").forGetter(DeadzoneGroup::name),
            Identifier.CODEC.listOf().fieldOf("axes").forGetter(DeadzoneGroup::axes)
    ).apply(instance, DeadzoneGroup::new));
}
