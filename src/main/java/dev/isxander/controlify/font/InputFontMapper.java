package dev.isxander.controlify.font;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.utils.CUtil;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputFontMapper implements SimpleResourceReloadListener<InputFontMapper.Preparations> {
    private ImmutableMap<String, BiDirectionalCharMap> mappings;

    private static final Codec<List<InputToChar>> INPUT_TO_CHAR_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC,
            Codec.STRING.comapFlatMap(
                    (str) -> {
                        if (str.length() != 1) {
                            return DataResult.error(() -> "Expected a single character string, got " + str);
                        }
                        return DataResult.success(str.charAt(0));
                    },
                    String::valueOf
            )
    ).xmap(
            map -> map.entrySet().stream().map(entry -> new InputToChar(entry.getKey(), entry.getValue())).toList(),
            mappings -> mappings.stream().collect(Collectors.toMap(InputToChar::input, InputToChar::character))
    );

    @Override
    public CompletableFuture<InputFontMapper.Preparations> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, Resource> mappingResources = manager.listResources("controllers/font_mappings", (rl) -> rl.getPath().endsWith(".json"));
            Map<String, List<InputToChar>> mappings = mappingResources.entrySet().stream().flatMap(entry -> {
                ResourceLocation rl = entry.getKey();
                Resource resource = entry.getValue();

                String themeName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(rl.getPath(), "/"), ".json");
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    List<InputToChar> maps = INPUT_TO_CHAR_CODEC.parse(JsonOps.INSTANCE, element)
                            .resultOrPartial(CUtil.LOGGER::error).orElse(null);
                    if (maps != null) {
                        return Stream.of(Pair.of(themeName, maps));
                    }
                } catch (Exception e) {
                    CUtil.LOGGER.error("Failed to load font mappings for theme {}", themeName, e);
                }

                return Stream.empty();
            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

            return new Preparations(mappings);
        });
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        ImmutableMap.Builder<String, BiDirectionalCharMap> builder = ImmutableMap.builder();
        data.mappings().forEach((theme, inputToChar) -> {
            builder.put(theme, new BiDirectionalCharMap(inputToChar));
        });
        mappings = builder.build();
        return CompletableFuture.completedFuture(null);
    }

    public BiDirectionalCharMap getMappings(String theme) {
        return mappings.get(theme);
    }

    public Component getComponentFromBinding(String theme, ControllerBinding binding) {
        String literal = binding.getBind().getRelevantInputs().stream()
                .map(input -> String.valueOf(getChar(theme, input)))
                .collect(Collectors.joining("+"));
        return Component.literal(literal).withStyle(style -> style.withFont(Controlify.id("controller/" + theme)));
    }

    public char getChar(String theme, ResourceLocation input) {
        BiDirectionalCharMap map = getMappings(theme);
        if (map != null) {
            return map.inputToChar().getOrDefault(input, '\0');
        }
        return '\0';
    }

    public ResourceLocation getInput(String theme, char character) {
        BiDirectionalCharMap map = getMappings(theme);
        if (map != null) {
            return map.charToInput().get(character);
        }
        return null;
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("controlify", "font_mappings");
    }

    public record Preparations(Map<String, List<InputToChar>> mappings) {

    }
    public record BiDirectionalCharMap(Map<ResourceLocation, Character> inputToChar, Map<Character, ResourceLocation> charToInput) {
        public BiDirectionalCharMap(List<InputToChar> inputToChar) {
            this(
                    inputToChar.stream().collect(Collectors.toMap(InputToChar::input, InputToChar::character)),
                    inputToChar.stream().collect(Collectors.toMap(InputToChar::character, InputToChar::input))
            );
        }
    }
    public record InputToChar(ResourceLocation input, char character) {}
}
