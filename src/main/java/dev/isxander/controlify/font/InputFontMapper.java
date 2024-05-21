package dev.isxander.controlify.font;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputFontMapper implements SimpleControlifyReloadListener<InputFontMapper.Preparations> {
    private ImmutableMap<ResourceLocation, FontMap> mappings;

    private static final Codec<Character> CHAR_CODEC = Codec.STRING.comapFlatMap(
            (str) -> {
                if (str.length() != 1) {
                    return DataResult.error(() -> "Expected a single character string, got " + str);
                }
                return DataResult.success(str.charAt(0));
            },
            String::valueOf
    );

    private static final Codec<FontMap> FONT_MAP_CODEC = Codec.pair(
            CHAR_CODEC.fieldOf("unknown").codec(),
            Codec.unboundedMap(ResourceLocation.CODEC, CHAR_CODEC)
    ).xmap(
            pair -> new FontMap(pair.getFirst(), pair.getSecond()),
            map -> Pair.of(map.unknown(), map.inputToChar())
    );

    private static final FileToIdConverter fileToIdConverter = FileToIdConverter.json("controllers/font_mappings");

    @Override
    public CompletableFuture<InputFontMapper.Preparations> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, Resource> mappingResources = fileToIdConverter.listMatchingResources(manager);
            Map<ResourceLocation, FontMap> mappings = mappingResources.entrySet().stream().flatMap(entry -> {
                ResourceLocation rl = entry.getKey();
                Resource resource = entry.getValue();

                ResourceLocation namespace = fileToIdConverter.fileToId(rl);
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    FontMap map = FONT_MAP_CODEC.parse(JsonOps.INSTANCE, element)
                            .resultOrPartial(CUtil.LOGGER::error).orElse(null);
                    if (map != null) {
                        return Stream.of(Pair.of(namespace, map));
                    }
                } catch (Exception e) {
                    CUtil.LOGGER.error("Failed to load font mappings for namespace {}", namespace, e);
                }

                return Stream.empty();
            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

            return new Preparations(mappings);
        });
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            ImmutableMap.Builder<ResourceLocation, FontMap> builder = ImmutableMap.builder();
            data.mappings().forEach(builder::put);
            mappings = builder.build();
        }, executor);
    }

    public FontMap getMappings(ResourceLocation namespace) {
        return mappings.get(namespace);
    }

    public Component getComponentFromBinding(ResourceLocation namespace, @Nullable InputBinding binding) {
        if (binding == null) {
            // TODO: implement some sort of "unbound" character
            return Component.literal("?");
        }

        List<ResourceLocation> relevantInputs = binding.boundInput().getRelevantInputs();
        return getComponentFromInputs(namespace, relevantInputs);
    }

    public Component getComponentFromBind(ResourceLocation namespace, Input input) {
        List<ResourceLocation> relevantInputs = input.getRelevantInputs();
        return getComponentFromInputs(namespace, relevantInputs);
    }

    public Component getComponentFromInputs(ResourceLocation namespace, List<ResourceLocation> inputs) {
        if (inputs.isEmpty()) {
            return Component.literal("<unbound>");
        }

        String literal = inputs.stream()
                .map(input -> String.valueOf(getChar(namespace, input)))
                .collect(Collectors.joining("+"));
        return Component.literal(literal).withStyle(style -> style.withFont(CUtil.addPrefix(namespace, "controller/")));
    }

    public char getChar(ResourceLocation namespace, ResourceLocation input) {
        FontMap map = getMappings(namespace);
        if (map != null) {
            return map.inputToChar().getOrDefault(input, map.unknown());
        }
        return '\0';
    }

    @Override
    public ResourceLocation getReloadId() {
        return new ResourceLocation("controlify", "font_mappings");
    }

    public record Preparations(Map<ResourceLocation, FontMap> mappings) {

    }
    public record FontMap(char unknown, Map<ResourceLocation, Character> inputToChar) {
    }
}
