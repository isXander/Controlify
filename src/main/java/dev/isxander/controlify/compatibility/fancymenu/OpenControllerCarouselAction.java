package dev.isxander.controlify.compatibility.fancymenu;

import de.keksuccino.fancymenu.customization.action.Action;
import dev.isxander.controlify.gui.screen.ControllerCarouselScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenControllerCarouselAction extends Action {
    public OpenControllerCarouselAction() {
        super("controlify:open-carousel");
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public void execute(@Nullable String s) {
        ControllerCarouselScreen.openConfigScreen(Minecraft.getInstance().screen);
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
