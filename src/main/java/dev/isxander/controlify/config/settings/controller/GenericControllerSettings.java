package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.api.guide.GuideVerbosity;
import dev.isxander.controlify.config.dto.controller.GenericControllerConfig;
import org.jspecify.annotations.Nullable;

public class GenericControllerSettings {
    public @Nullable String nickname;
    public boolean autoJump;
    public boolean toggleSprint;
    public boolean toggleSneak;
    public boolean disableFlyDrifting;
    public GuideSettings guide;
    public KeyboardSettings keyboard;

    public GenericControllerSettings(
            @Nullable String nickname,
            boolean autoJump,
            boolean toggleSprint,
            boolean toggleSneak,
            boolean disableFlyDrifting,
            GuideSettings guide,
            KeyboardSettings keyboard
    ) {
        this.nickname = nickname;
        this.autoJump = autoJump;
        this.toggleSprint = toggleSprint;
        this.toggleSneak = toggleSneak;
        this.disableFlyDrifting = disableFlyDrifting;
        this.guide = guide;
        this.keyboard = keyboard;
    }

    public static GenericControllerSettings fromDTO(GenericControllerConfig dto) {
        return new GenericControllerSettings(
                dto.nickname().orElse(null),
                dto.autoJump(),
                dto.toggleSprint(),
                dto.toggleSneak(),
                dto.disableFlyDrifting(),
                GuideSettings.fromDTO(dto.guide()),
                KeyboardSettings.fromDTO(dto.keyboard())
        );
    }

    public GenericControllerConfig toDTO() {
        return new GenericControllerConfig(
                java.util.Optional.ofNullable(nickname),
                autoJump,
                toggleSprint,
                toggleSneak,
                disableFlyDrifting,
                guide.toDTO(),
                keyboard.toDTO()
        );
    }

    public static class GuideSettings {
        public GuideVerbosity verbosity;
        public boolean showIngameGuide;
        public boolean ingameGuideButtom;
        public boolean showScreenGuides;

        public GuideSettings(
                GuideVerbosity verbosity,
                boolean showIngameGuide,
                boolean ingameGuideButtom,
                boolean showScreenGuides
        ) {
            this.verbosity = verbosity;
            this.showIngameGuide = showIngameGuide;
            this.ingameGuideButtom = ingameGuideButtom;
            this.showScreenGuides = showScreenGuides;
        }

        public static GuideSettings fromDTO(GenericControllerConfig.GuideConfig dto) {
            return new GuideSettings(
                    dto.verbosity(),
                    dto.showIngameGuide(),
                    dto.ingameGuideButtom(),
                    dto.showScreenGuides()
            );
        }

        public GenericControllerConfig.GuideConfig toDTO() {
            return new GenericControllerConfig.GuideConfig(
                    verbosity,
                    showIngameGuide,
                    ingameGuideButtom,
                    showScreenGuides
            );
        }
    }

    public static class KeyboardSettings {
        public boolean showOnScreenKeyboard;
        public boolean hintCursor;
        public boolean hintCommandSuggester;
        public boolean hintSignLine;

        public KeyboardSettings(
                boolean showOnScreenKeyboard,
                boolean hintCursor,
                boolean hintCommandSuggester,
                boolean hintSignLine
        ) {
            this.showOnScreenKeyboard = showOnScreenKeyboard;
            this.hintCursor = hintCursor;
            this.hintCommandSuggester = hintCommandSuggester;
            this.hintSignLine = hintSignLine;
        }

        public static KeyboardSettings fromDTO(GenericControllerConfig.KeyboardConfig dto) {
            return new KeyboardSettings(
                    dto.showOnScreenKeyboard(),
                    dto.hintCursor(),
                    dto.hintCommandSuggester(),
                    dto.hintSignLine()
            );
        }

        public GenericControllerConfig.KeyboardConfig toDTO() {
            return new GenericControllerConfig.KeyboardConfig(
                    showOnScreenKeyboard,
                    hintCursor,
                    hintCommandSuggester,
                    hintSignLine
            );
        }
    }
}
