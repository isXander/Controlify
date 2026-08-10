package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.server.ControlifyServer;
import dev.isxander.controlify.server.ControlifyServerConfig;
import dev.isxander.controlify.server.ServerPolicies;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.server.MinecraftServer;

@SuppressWarnings("UnstableApiUsage")
public class ServerPolicyTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		controlify.resetSettings();

		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			// dedicated-like server that runs in the same process and connects like LAN.
			// server initializers do not run, which means controlify will not send server policy packets
			try (var world = context.worldBuilder().createServer()) {
				try (var connection = world.connect()) {
					context.waitFor(minecraft -> CTestUtil.isToastPresent(minecraft, "controlify.toast.new_server.title"));

					connection.getClientLevel().waitForChunksDownload();

					assertKeyboardMovement(context, true);
				}
			}

			// server that allows analogue movement should be enabled by default
			try (var world = context.worldBuilder().createServer()) {

				try (var connection = world.connect()) {
					var config = new ControlifyServerConfig();
					config.allowAnalogueMovement = true;

					world.runOnServer(server -> {
						var player = CTestUtil.getPrincipalPlayer(server);
						ControlifyServer.setServerPolicies(player, config);
					});

					context.waitTick();

					assertKeyboardMovement(context, false);
				}

			}

			// Ensure that analogue movement is DISABLED if the server policy states so
			// despite the current server being on the client-side whitelist
			try (var world = context.worldBuilder().createServer()) {
				int port = world.computeOnServer(MinecraftServer::getPort);
				String serverIp = "localhost:" + port;

				context.runOnClient(_ -> {
					Controlify.instance().config().getSettings().globalSettings().analogueMovementWhitelist.add(serverIp);
				});

				try (var connection = world.connect()) {
					var config = new ControlifyServerConfig();
					config.allowAnalogueMovement = false;

					world.runOnServer(server -> {
						var player = CTestUtil.getPrincipalPlayer(server);
						ControlifyServer.setServerPolicies(player, config);
					});

					context.waitTick();

					assertKeyboardMovement(context, true);
				}
			}
		}

		controlify.resetSettings();
	}

	private void assertKeyboardMovement(ClientGameTestContext context, boolean requiredKeyboardMovement) {
		boolean observedKeyboardMovement = context.computeOnClient(_ -> {
			return Controlify.instance().config().getSettings().globalSettings().shouldUseKeyboardMovement();
		});
		if (requiredKeyboardMovement != observedKeyboardMovement) {
			throw new AssertionError("Expected keyboard movement to be " + requiredKeyboardMovement + " but found " + observedKeyboardMovement);
		}
	}
}
