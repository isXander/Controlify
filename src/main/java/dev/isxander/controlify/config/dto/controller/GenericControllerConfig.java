package dev.isxander.controlify.config.dto.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.guide.GuideVerbosity;

import java.util.Optional;

public record GenericControllerConfig(
        Optional<String> nickname,
        boolean autoJump,
        boolean toggleSprint,
        boolean toggleSneak,
        boolean disableFlyDrifting,
        GuideConfig guide,
        KeyboardConfig keyboard
) {
    public static final Codec<GenericControllerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("nickname").forGetter(GenericControllerConfig::nickname),
            Codec.BOOL.fieldOf("auto_jump").forGetter(GenericControllerConfig::autoJump),
            Codec.BOOL.fieldOf("toggle_sprint").forGetter(GenericControllerConfig::toggleSprint),
            Codec.BOOL.fieldOf("toggle_sneak").forGetter(GenericControllerConfig::toggleSneak),
            Codec.BOOL.fieldOf("disable_fly_drifting").forGetter(GenericControllerConfig::disableFlyDrifting),
            GuideConfig.CODEC.fieldOf("guide").forGetter(GenericControllerConfig::guide),
            KeyboardConfig.CODEC.fieldOf("keyboard").forGetter(GenericControllerConfig::keyboard)
    ).apply(instance, GenericControllerConfig::new));

    public record GuideConfig(
            GuideVerbosity verbosity,
            boolean showIngameGuide,
            boolean ingameGuideButtom,
            boolean showScreenGuides
    ) {
        public static final Codec<GuideConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GuideVerbosity.CODEC.fieldOf("verbosity").forGetter(GuideConfig::verbosity),
                Codec.BOOL.fieldOf("show_ingame_guide").forGetter(GuideConfig::showIngameGuide),
                Codec.BOOL.fieldOf("ingame_guide_buttom").forGetter(GuideConfig::ingameGuideButtom),
                Codec.BOOL.fieldOf("show_screen_guides").forGetter(GuideConfig::showScreenGuides)
        ).apply(instance, GuideConfig::new));
    }

    public record KeyboardConfig(
            boolean showOnScreenKeyboard,
            boolean hintCursor,
            boolean hintCommandSuggester,
            boolean hintSignLine
    ) {
        public static final Codec<KeyboardConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("show_on_screen_keyboard").forGetter(KeyboardConfig::showOnScreenKeyboard),
                Codec.BOOL.fieldOf("hint_cursor").forGetter(KeyboardConfig::hintCursor),
                Codec.BOOL.fieldOf("hint_command_suggester").forGetter(KeyboardConfig::hintCommandSuggester),
                Codec.BOOL.fieldOf("hint_sign_line").forGetter(KeyboardConfig::hintSignLine)
        ).apply(instance, KeyboardConfig::new));
    }
}
