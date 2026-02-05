package dev.isxander.splitscreen.client.integrations;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.utils.ToastUtils;
import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.SplitscreenPawn;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchException;
import dev.isxander.splitscreen.client.host.gui.ControlifySplitscreenSettingsScreen;
import dev.isxander.splitscreen.util.CSUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

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
    public void onControlifyPreInit(PreInitContext ctx) {
        ADD_PLAYER_BIND = ctx.bindings().registerBinding(builder -> builder
                .id(CSUtil.rl("add_player"))
                .category(BINDING_CATEGORY));

        ControlifyEvents.CONTROLLER_STATE_UPDATE.register(this::onControllerStateUpdate);
        ControlifyEvents.FINISHED_INIT.register(this::onFinishedInit);
        ControlifyEvents.CONTROLLER_CONNECTED.register(this::onControllerConnected);
        ControlifyEvents.CONTROLLER_DISCONNECTED.register(this::onControllerDisconnected);
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
    }

    @Override
    public void onControlifyInit(InitContext ctx) {

    }

    private void onControllerStateUpdate(ControlifyEvents.ControllerStateUpdate event) {
        ControllerEntity controller = event.controller();
        Controlify controlify = Controlify.instance();

        // since this extension runs on all pawns, this logic should only happen on the host, where isRelaunched is false
        if (!isRelaunched) {
            if (ADD_PLAYER_BIND.on(controller).justTapped()) {
                boolean thisControllerAvailable = getAvailableControllers().anyMatch(c -> c == controller);

                int controllersConnected = controlify
                        .getControllerManager().orElseThrow()
                        .getConnectedControllers().size();

                if (thisControllerAvailable) {
                    if (Minecraft.getInstance().screen instanceof ControlifySplitscreenSettingsScreen settingsScreen) {
                        settingsScreen.onAvailableControllerPressJoin(controller, controllersConnected == 1);
                    }
                }
            }
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

    public static Stream<ControllerEntity> getAvailableControllers() {
        // Get all connected controller uids that are already being used by players
        Collection<ControllerUID> usedControllers = SplitscreenBootstrapper.getController()
                .map(c -> c.getPawns().stream() // if splitscreen is active, get the input methods of all current players
                        .map(SplitscreenPawn::getAssociatedInputMethod)
                        .flatMap(inputMethod -> inputMethod.getControllerUID().stream()))
                .orElse(Stream.empty())
                .toList();

        // Get all controllers that are not already being used
        return Controlify.instance().getControllerManager()
                .map(m -> m.getConnectedControllers().stream())
                .orElse(Stream.empty())
                .filter(controller -> !usedControllers.contains(controller.uid()));
    }
}
