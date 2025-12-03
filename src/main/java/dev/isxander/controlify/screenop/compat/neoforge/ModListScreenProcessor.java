//? if neoforge {
/*package dev.isxander.controlify.screenop.compat.neoforge;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.platform.neoforge.mixins.ModListScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.Util;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class ModListScreenProcessor extends ScreenProcessor<ModListScreen> {
    public ModListScreenProcessor(ModListScreen screen) {
        super(screen);
    }

    final InputBindingSupplier OPEN_MOD_CONFIG = ControlifyBindings.GUI_PRESS;
    final InputBindingSupplier OPEN_MODS_FOLDER = ControlifyBindings.GUI_ABSTRACT_ACTION_1;
    final InputBindingSupplier NAVIGATE_BACK = ControlifyBindings.GUI_BACK;

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);

        if (OPEN_MOD_CONFIG.on(controller).guiPressed().get()) {
            getAccessor().invokeDisplayModConfig();
            playClackSound();
        }
        if (OPEN_MODS_FOLDER.on(controller).guiPressed().get()) {
            openModsFolder();
            playClackSound();
        }
    }

    private ModListScreenAccessor getAccessor() {
        return ((ModListScreenAccessor) (Object) this.screen);
    }

    private void openModsFolder() {
        Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile());
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();
        final ModListScreenAccessor accessor = getAccessor();

        ButtonGuideApi.addGuideToButton(
                accessor.getConfigButton(),
                OPEN_MOD_CONFIG,
                ButtonGuidePredicate.always()
        );
        ButtonGuideApi.addGuideToButton(
                accessor.getOpenModsFolderButton(),
                OPEN_MODS_FOLDER,
                ButtonGuidePredicate.always()
        );
        ButtonGuideApi.addGuideToButton(
                accessor.getDoneButton(),
                NAVIGATE_BACK,
                ButtonGuidePredicate.always()
        );
    }
}*/
//?}
