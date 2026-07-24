package dev.isxander.controlify.neoforge;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.screen.ModConfigOpenerScreen;
import dev.isxander.controlify.neoforge.platform.NeoforgePlatformMainImpl;
import dev.isxander.controlify.neoforge.platform.client.NeoforgePlatformClientImpl;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.server.ControlifyServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod("controlify")
public class ControlifyBootstrap {
	public ControlifyBootstrap(IEventBus modBus) {
		PlatformMainUtil.IMPL = new NeoforgePlatformMainImpl();
		ControlifyServer.getInstance().onInitialize();

		if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
			PlatformClientUtil.IMPL = new NeoforgePlatformClientImpl();

			ModLoadingContext.get().registerExtensionPoint(
				net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
				() -> (client, parent) -> new ModConfigOpenerScreen(parent)
			);

			Controlify.instance().preInitialiseControlify();
		}
		if (PlatformMainUtil.getEnv() == Environment.SERVER) {
			ControlifyServer.getInstance().onInitializeServer();
		}
	}
}
