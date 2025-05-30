package dev.isxander.splitscreen;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.relauncher.RelaunchArguments;
import dev.isxander.splitscreen.relauncher.RelaunchException;
import dev.isxander.splitscreen.util.CSUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ControlifyExtension implements ControlifyEntrypoint {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component BINDING_CATEGORY = Component.translatable("controlify.splitscreen.bind.category");

    public static InputBindingSupplier ADD_PLAYER_BIND;

    private @Nullable ControllerEntity controller;

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {

    }

    @Override
    public void onControlifyInit(ControlifyApi controlifyApi) {
        ADD_PLAYER_BIND = ControlifyBindApi.get().registerBinding(builder -> builder
                .id(CSUtil.rl("add_player"))
                .category(BINDING_CATEGORY));

        ControlifyEvents.CONTROLLER_STATE_UPDATE.register(this::onControllerStateUpdate);
        ControlifyEvents.FINISHED_INIT.register(this::onFinishedInit);
        ControlifyEvents.CONTROLLER_CONNECTED.register(this::onControllerConnected);
        ControlifyEvents.CONTROLLER_DISCONNECTED.register(this::onControllerDisconnected);
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
    }

    private void onControllerStateUpdate(ControlifyEvents.ControllerStateUpdate event) {
        ControllerEntity controller = event.controller();
        Controlify controlify = Controlify.instance();

        SplitscreenBootstrapper.getController().ifPresent(splitController -> {
            controller.input().ifPresent(input -> {
                if (ADD_PLAYER_BIND.on(controller).justPressed()) {
                    int controllersConnected = Controlify.instance()
                            .getControllerManager().orElseThrow()
                            .getConnectedControllers().size();
                    boolean isCurrentController = controller == controlify.getCurrentController().orElse(null);

                    if (controllersConnected >= 2 && !isCurrentController) {
                        splitController.summonNewPawnClient(controller.uid());
                    }
                }
            });
        });
    }

    private void onFinishedInit(ControlifyEvents.FinishedInit event) {
        if (RelaunchArguments.CONTROLLER.get().isPresent() && this.controller == null) {
            var exception = new RelaunchException("Relaunched client could not find controller it is associated to!");

            CrashReport report = CrashReport.forThrowable(exception, "Relaunch failed");
            throw new ReportedException(report);
        }
    }

    private void onControllerConnected(ControlifyEvents.ControllerConnected event) {
        ControllerUID relaunchController = RelaunchArguments.CONTROLLER.get().orElse(null);
        if (event.controller().uid().equals(relaunchController)) {
            LOGGER.info("Our bound controller {} has been connected!", relaunchController);
            this.controller = event.controller();
        }
    }

    private void onControllerDisconnected(ControlifyEvents.ControllerDisconnected event) {
        if (event.controller() == this.controller) {
            LOGGER.warn("Controller {} assciated with this client has been disconnected! This client is now closing.", event.controller().uid());
            this.controller = null;

            Minecraft.getInstance().stop();
        }
    }

    private void onClientTick(Minecraft minecraft) {
        if (this.controller != null) {
            // this client is solely bound to this controller, and should never be anything else
            Controlify.instance().setCurrentController(this.controller, true);
        }
    }
}
