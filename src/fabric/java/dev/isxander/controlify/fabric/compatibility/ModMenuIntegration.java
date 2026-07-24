//? if mod_menu {
package dev.isxander.controlify.fabric.compatibility;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.controlify.gui.screen.ControlifySettingsScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ControlifySettingsScreen::new;
    }
}
//?}
