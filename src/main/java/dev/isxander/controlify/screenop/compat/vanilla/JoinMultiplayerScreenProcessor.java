package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.CommonComponents;

import java.util.function.Supplier;

public class JoinMultiplayerScreenProcessor extends ScreenProcessor<JoinMultiplayerScreen> {
    private final Supplier<ServerSelectionList> listSupplier;

    public JoinMultiplayerScreenProcessor(JoinMultiplayerScreen screen, Supplier<ServerSelectionList> listSupplier) {
        super(screen);
        this.listSupplier = listSupplier;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (controller.bindings().GUI_BACK.justPressed()) {
            this.getWidget(CommonComponents.GUI_BACK).ifPresent(back -> {
                if (!back.isFocused()) {
                    ServerSelectionList list = listSupplier.get();
                    list.setSelected(null);
                    list.setFocused(null);

                    screen.setFocused(back);
                } else {
                    ((AbstractButton) back).onPress();
                }
            });
        }

        super.handleButtons(controller);
    }

    @Override
    public void onWidgetRebuild() {
        this.getWidget(CommonComponents.GUI_BACK).ifPresent(button -> {
            ButtonGuideApi.addGuideToButton((AbstractButton) button, controller -> controller.bindings().GUI_BACK, ButtonGuidePredicate.ALWAYS);
        });

        super.onWidgetRebuild();
    }
}
