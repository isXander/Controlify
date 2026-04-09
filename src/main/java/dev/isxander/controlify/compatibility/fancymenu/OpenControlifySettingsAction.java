//? if fancy_menu {
package dev.isxander.controlify.compatibility.fancymenu;

import de.keksuccino.fancymenu.customization.action.Action;
import dev.isxander.controlify.gui.screen.ControlifySettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenControlifySettingsAction extends Action {
    public OpenControlifySettingsAction() {
        super("controlify:open-settings");
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public void execute(@Nullable String s) {
        ControlifySettingsScreen.openScreen(Minecraft.getInstance().screen);
    }

    @Override
    public @NotNull Component getActionDisplayName() {
        return Component.translatable("controlify.gui.button");
    }

    @Override
    public @NotNull Component[] getActionDescription() {
        return new Component[0];
    }

    @Override
    public @Nullable Component getValueDisplayName() {
        return null;
    }

    @Override
    public @Nullable String getValueExample() {
        return "";
    }
}
//?}
