package dev.isxander.splitscreen.client.integrations;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.utils.ToastUtils;
import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchException;
import dev.isxander.splitscreen.util.CSUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class ControlifyExtension implements ControlifyEntrypoint {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component BINDING_CATEGORY = Component.translatable("controlify.splitscreen.bind.category");

    private static final boolean isRelaunched = RelaunchArguments.RELAUNCHED.get().orElse(false);
    private static final @Nullable InputMethod relaunchedInputMethod = RelaunchArguments.INPUT_METHOD.get().orElse(null);

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

        // since this extension runs on all pawns, this logic should only happen on the host, where isRelaunched is false
        if (!isRelaunched) {
            controller.input().ifPresent(input -> {
                int controllersConnected = Controlify.instance()
                        .getControllerManager().orElseThrow()
                        .getConnectedControllers().size();

                ControllerEntity currentController = controlify.getCurrentController().orElse(null);

                if (ADD_PLAYER_BIND.on(controller).longPressed(true)) {
                    if (controllersConnected == 1) {
                        // in the case where there is only one controller connected, we initialise
                        // splitscreen as Player 1: KB&M, Player 2: Controller 1

                        // bootstrap if we haven't already bootstrapped
                        if (!SplitscreenBootstrapper.isSplitscreen()) {
                            SplitscreenBootstrapper.boostrapAsController(Minecraft.getInstance(), InputMethod.keyboardAndMouse());
                        }

                        controlify.setCurrentController(null, true);

                        SplitscreenBootstrapper.getController().orElseThrow()
                                .summonNewPawnClient(InputMethod.controller(controller.uid()));
                    } else if (controllersConnected >= 2 && controller != currentController) {
                        // when there are more than one controller connected, and the controller
                        // that is not in use attempts to join, just do a simple 2 controller splitscreen

                        ControllerEntity player1 = currentController;
                        ControllerEntity playerX = controller;

                        // bootstrap if we haven't already bootstrapped
                        if (!SplitscreenBootstrapper.isSplitscreen()) {
                            SplitscreenBootstrapper.boostrapAsController(Minecraft.getInstance(), InputMethod.controller(player1.uid()));
                        }

                        SplitscreenBootstrapper.getController().orElseThrow()
                                .summonNewPawnClient(InputMethod.controller(playerX.uid()));
                    }
                } else if (ADD_PLAYER_BIND.on(controller).justTapped()) {
                    if (controllersConnected >= 2 && controller != currentController) {
                        // if they tried to join but only with a tap, tell them they need to hold the button
                        // so they learn the behaviour
                        InputBinding binding = ADD_PLAYER_BIND.on(controller);

                        ToastUtils.sendToast(
                                Component.translatable("controlify.splitscreen.toast.long_press_to_join.title"),
                                Component.translatable("controlify.splitscreen.toast.long_press_to_join", binding.inputIcon()),
                                true
                        );
                    }
                }
            });
        }
    }

    private void onFinishedInit(ControlifyEvents.FinishedInit event) {
        if (relaunchedInputMethod != null && relaunchedInputMethod.isController() && this.controller == null) {
            var exception = new RelaunchException("Relaunched client could not find controller it is associated to!");

            CrashReport report = CrashReport.forThrowable(exception, "Relaunch failed");
            throw new ReportedException(report);
        }
    }

    private void onControllerConnected(ControlifyEvents.ControllerConnected event) {
        ControllerUID relaunchController = Optional.ofNullable(relaunchedInputMethod)
                .flatMap(InputMethod::getControllerUID)
                .orElse(null);
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
