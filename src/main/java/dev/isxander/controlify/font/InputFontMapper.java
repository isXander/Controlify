package dev.isxander.controlify.font;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputFontMapper implements SimpleControlifyReloadListener<InputFontMapper.Preparations> {
    private ImmutableMap<Identifier, FontMap> mappings = ImmutableMap.of();
    private FontMap defaultFontMap;

    private static final Codec<Character> CHAR_CODEC = Codec.STRING.comapFlatMap(
            (str) -> {
                if (str.length() != 1) {
                    return DataResult.error(() -> "Expected a single character string, got " + str);
                }
                return DataResult.success(str.charAt(0));
            },
            String::valueOf
    );

    private static final Codec<Pair<Character, Map<Identifier, Character>>> FONT_MAP_CODEC = Codec.pair(
            CHAR_CODEC.fieldOf("unknown").codec(),
            Codec.unboundedMap(Identifier.CODEC, CHAR_CODEC)
    );

    private static final FileToIdConverter fileToIdConverter = FileToIdConverter.json("controllers/font_mappings");

    @Override
    public CompletableFuture<InputFontMapper.Preparations> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, Resource> mappingResources = fileToIdConverter.listMatchingResources(manager);
            Map<Identifier, FontMap> mappings = mappingResources.entrySet().stream().flatMap(entry -> {
                Identifier rl = entry.getKey();
                Resource resource = entry.getValue();

                Identifier namespace = fileToIdConverter.fileToId(rl);
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    FontMap map = FONT_MAP_CODEC.parse(JsonOps.INSTANCE, element)
                            .resultOrPartial(CUtil.LOGGER::error)
                            .map(pair -> new FontMap(namespace, pair.getFirst(), pair.getSecond()))
                            .orElse(null);
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
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            ImmutableMap.Builder<Identifier, FontMap> builder = ImmutableMap.builder();
            data.mappings().forEach(builder::put);
            mappings = builder.build();
            defaultFontMap = mappings.get(ControllerType.DEFAULT.namespace());
        }, executor);
    }

    public FontMap getMappings(Identifier namespace) {
        return mappings.getOrDefault(namespace, defaultFontMap);
    }

    public Component getComponentFromBinding(Identifier namespace, @Nullable InputBinding binding) {
        if (binding == null) {
            // TODO: implement some sort of "unbound" character
            return Component.literal("?");
        }

        List<Identifier> relevantInputs = binding.boundInput().getRelevantInputs();
        return getComponentFromInputs(namespace, relevantInputs);
    }

    public Component getComponentFromBind(Identifier namespace, Input input) {
        List<Identifier> relevantInputs = input.getRelevantInputs();
        return getComponentFromInputs(namespace, relevantInputs);
    }

    public Component getComponentFromInputs(Identifier namespace, List<Identifier> inputs) {
        if (inputs.isEmpty()) {
            return Component.literal("<unbound>");
        }

        FontMap fontMap = getMappings(namespace);

        String literal = inputs.stream()
                .map(input -> String.valueOf(getChar(fontMap, input)))
                .collect(Collectors.joining("+"));

        return Component.literal(literal).withStyle(style -> style
                .withFont(CUtil.createResourceFont(fontMap.namespace().withPrefix("impl/")))
                //? if >=1.21.4
                .withShadowColor(0x00000000) // remove shadow
                .withColor(0xFFFFFFFF)); // override color of font renderer so the glyph always renders properly
    }

    private char getChar(FontMap fontMap, Identifier input) {
        Character ch = fontMap.inputToChar().get(input);

        // fallback to default icon set if there is no mapping
        if (ch == null) {
            ch = defaultFontMap.inputToChar().getOrDefault(input, fontMap.unknown());
        }

        return ch;
    }

    @Override
    public Identifier getReloadId() {
        return CUtil.rl("font_mappings");
    }

    public record Preparations(Map<Identifier, FontMap> mappings) {

    }
    public record FontMap(Identifier namespace, char unknown, Map<Identifier, Character> inputToChar) {
    }
}
