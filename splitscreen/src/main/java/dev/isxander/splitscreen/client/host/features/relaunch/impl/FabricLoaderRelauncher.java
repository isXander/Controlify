package dev.isxander.splitscreen.client.host.features.relaunch.impl;

import dev.isxander.splitscreen.client.host.features.relaunch.LaunchInfo;
import dev.isxander.splitscreen.client.host.features.relaunch.RelaunchUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.knot.KnotClient;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FabricLoaderRelauncher {
    private static LaunchInfo launchInfo;

    public static LaunchInfo getLaunchInfo() {
        if (launchInfo == null) {
            launchInfo = generateLaunchInfo();
        }
        return launchInfo;
    }

    private static LaunchInfo generateLaunchInfo() {
        Path javaExecutable = RelaunchUtil.findJavaExecutable();
        List<String> gameArgs = Stream.of(FabricLoader.getInstance().getLaunchArguments(false))
                .map(RelaunchUtil::quoteArg)
                .toList();
        String mainClass = KnotClient.class.getName();
        List<String> jvmArgs = RelaunchUtil.findJVMArgs();
        String classpath = RelaunchUtil.findClasspath();
        Path workingDirectory = RelaunchUtil.findWorkingDirectory();

        return new LaunchInfo(
                javaExecutable,
                jvmArgs,
                classpath,
                mainClass,
                gameArgs,
                workingDirectory
        );
    }
}
