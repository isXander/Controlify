package dev.isxander.controlify.splitscreen.host.gui;

import dev.isxander.controlify.splitscreen.config.AudioMethod;
import dev.isxander.controlify.splitscreen.config.MusicMethod;
import dev.isxander.controlify.splitscreen.config.SplitscreenConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class SplitscreenConfigGuiFactory {
    public static Screen buildScreen(@Nullable Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("controlify.splitscreen.config.title"))
                .category(buildAudioCategory())
                .build().generateScreen(parent);
    }

    private static ConfigCategory buildAudioCategory() {
        SplitscreenConfig config = config();

        return ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.splitscreen.config.audio"))
                .option(
                        startOption(config.audioMethod, "audio", (builder, translationKey) -> builder
                                .controller(opt -> CyclingListControllerBuilder.create(opt)
                                        .values(AudioMethod.values())
                                        .formatValue(method -> Component.translatable(translationKey + "." + method.getSerializedName())))
                        ).build())
                .option(
                        startOption(config.musicMethod, "audio", (builder, translationKey) -> builder
                                .controller(opt -> CyclingListControllerBuilder.create(opt)
                                        .values(MusicMethod.values())
                                        .formatValue(method -> Component.translatable(translationKey + "." + method.getSerializedName())))
                        ).build()
                )
                .build();
    }

    private static SplitscreenConfig config() {
        return SplitscreenConfig.INSTANCE;
    }

    private static <T, U> U startOption(ConfigEntry<T> entry, String category, BiFunction<Option.Builder<T>, String, U> builder) {
        String translationKey = "controlify.splitscreen.config." + category + "." + entry.fieldName();
        var start = Option.<T>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".desc")))
                .binding(entry.asBinding());

        return builder.apply(start, translationKey);
    }

    private static <T> Option.Builder<T> startOption(ConfigEntry<T> entry, String category) {
        return startOption(entry, category, (opt, translationKey) -> opt);
    }
}
