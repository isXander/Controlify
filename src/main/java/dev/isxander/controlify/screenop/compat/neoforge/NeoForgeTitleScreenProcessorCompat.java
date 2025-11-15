//? if neoforge {
/*package dev.isxander.controlify.screenop.compat.neoforge;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.NotNull;
import net.neoforged.neoforge.client.gui.ModListScreen;

import java.util.function.Function;

public final class NeoForgeTitleScreenProcessorCompat {
    private final @NotNull Function<String, AbstractButton> getButtonByTranslationKey;
    private final ScreenProcessor<TitleScreen> screenProcessor;

    public NeoForgeTitleScreenProcessorCompat(@NotNull Function<String, AbstractButton> getButtonByTranslationKey, ScreenProcessor<TitleScreen> screenProcessor) {
        this.getButtonByTranslationKey = getButtonByTranslationKey;
        this.screenProcessor = screenProcessor;
    }

    private static final String FORGE_MODS_BUTTON_KEY = "fml.menu.mods";

    private AbstractButton getForgeModsButton() {
        return getButtonByTranslationKey.apply(FORGE_MODS_BUTTON_KEY);
    }

    private static final InputBindingSupplier OPEN_MOD_LIST_BINDING = ControlifyBindings.GUI_ABSTRACT_ACTION_2;

    public void onHandleButtons(@NotNull ControllerEntity controller) {
        if (OPEN_MOD_LIST_BINDING.on(controller).guiPressed().get()) {
            Minecraft.getInstance().setScreen(new ModListScreen(this.screenProcessor.screen));
            ScreenProcessor.playClackSound();
        }
    }

    public void onAddGuides() {
        ButtonGuideApi.addGuideToButton(
                getForgeModsButton(),
                OPEN_MOD_LIST_BINDING,
                ButtonGuidePredicate.always()
        );
    }
}
*///?}
