package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record FactDefinition(
		Identifier id,
		ContextualPredicate predicate
) {
	public static final Codec<List<FactDefinition>> LIST_CODEC = Codec.unboundedMap(Identifier.CODEC, ContextualPredicate.CODEC)
			.xmap(
					map -> map.entrySet().stream()
							.map(entry -> new FactDefinition(entry.getKey(), entry.getValue()))
							.toList(),
					defs -> defs.stream()
							.collect(Collectors.toMap(
									FactDefinition::id,
									FactDefinition::predicate,
									(first, second) -> second,
									LinkedHashMap::new
							))
			);

	public static Map<Identifier, FactDefinition> toMap(List<FactDefinition> list) {
		return list.stream()
				.collect(Collectors.toMap(
						FactDefinition::id,
						Function.identity(),
						(first, second) -> second,
						LinkedHashMap::new
				));
	}
}
