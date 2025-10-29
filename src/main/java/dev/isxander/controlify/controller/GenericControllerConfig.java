package dev.isxander.controlify.controller;

import dev.isxander.controlify.api.guide.GuideVerbosity;
import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericControllerConfig implements ConfigClass {
    public static final ResourceLocation ID = CUtil.rl("config/generic");

    @Nullable
    public String nickname;

    public boolean autoJump;
    public boolean toggleSprint;
    public boolean toggleSneak;
    public boolean disableFlyDrifting;

    public GuideVerbosity guideVerbosity;
    public boolean showIngameGuide;
    public boolean ingameGuideBottom;
    public boolean showScreenGuides;

    public boolean showOnScreenKeyboard;

    public boolean dontShowControllerSubmission;

    public boolean hintKeyboardCursor;
    public boolean hintKeyboardCommandSuggester;
    public boolean hintKeyboardSignLine;

    @Override
    public void save(ValueOutput output, ControllerEntity controller) {
        if (nickname != null) {
            output.putString("nickname", nickname);
        }

        output.putBoolean("auto_jump", autoJump);
        output.putBoolean("toggle_sprint", toggleSprint);
        output.putBoolean("toggle_sneak", toggleSneak);
        output.putBoolean("disable_fly_drifting", disableFlyDrifting);
        output.put("guide_verbosity", GuideVerbosity.CODEC, guideVerbosity);
        output.putBoolean("show_ingame_guide", showIngameGuide);
        output.putBoolean("ingame_guide_bottom", ingameGuideBottom);
        output.putBoolean("show_screen_guides", showScreenGuides);
        output.putBoolean("show_on_screen_keyboard", showOnScreenKeyboard);
        output.putBoolean("dont_show_controller_submission", dontShowControllerSubmission);
        output.putBoolean("hint_keyboard_cursor", hintKeyboardCursor);
        output.putBoolean("hint_keyboard_command_suggester", hintKeyboardCommandSuggester);
        output.putBoolean("hint_keyboard_sign_line", hintKeyboardSignLine);
    }

    @Override
    public void load(ValueInput input, ControllerEntity controller) {
        nickname = input.readStringOr("nickname", null);
        autoJump = input.readBooleanOr("auto_jump", false);
        toggleSprint = input.readBooleanOr("toggle_sprint", true);
        toggleSneak = input.readBooleanOr("toggle_sneak", true);
        disableFlyDrifting = input.readBooleanOr("disable_fly_drifting", false);
        guideVerbosity = input.readOr("guide_verbosity", GuideVerbosity.CODEC, GuideVerbosity.FULL);
        showIngameGuide = input.readBooleanOr("show_ingame_guide", true);
        ingameGuideBottom = input.readBooleanOr("ingame_guide_bottom", false);
        showScreenGuides = input.readBooleanOr("show_screen_guides", true);
        showOnScreenKeyboard = input.readBooleanOr("show_on_screen_keyboard", true);
        dontShowControllerSubmission = input.readBooleanOr("dont_show_controller_submission", false);
        hintKeyboardCursor = input.readBooleanOr("hint_keyboard_cursor", true);
        hintKeyboardCommandSuggester = input.readBooleanOr("hint_keyboard_command_suggester", true);
        hintKeyboardSignLine = input.readBooleanOr("hint_keyboard_sign_line", true);
    }
}
