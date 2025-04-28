package dev.isxander.controlify.splitscreen.host.relaunch.impl;

import dev.isxander.controlify.splitscreen.host.relaunch.LaunchInfo;
import dev.isxander.controlify.splitscreen.relauncher.RelaunchException;
import dev.isxander.controlify.splitscreen.host.relaunch.RelaunchUtil;

import java.nio.file.Path;
import java.util.List;

public class PrismRelauncher {
    private static final boolean IS_PRISM = System.getProperty("org.prismlauncher.launch.mainclass") != null;
    private static LaunchInfo launchInfo = null;

    public static boolean isPrism() {
        return IS_PRISM;
    }

    public static LaunchInfo getLaunchInfo() {
        if (launchInfo == null) {
            launchInfo = createLaunchInfo();
        }
        return launchInfo;
    }

    private static LaunchInfo createLaunchInfo() {
        if (!isPrism()) {
            throw new RelaunchException("Attempted to use Prism method for launch info, but not using Prism");
        }

        // the main class prism's entrypoint is proxying
        String mainClass = System.getProperty("org.prismlauncher.launch.mainclass");
        // the program arguments prism is passing to the main class
        // delimited by \u001F, the ASCII unit separator
        List<String> gameArgs = List.of(System.getProperty("org.prismlauncher.launch.gameargs").split("\u001F"));

        List<String> jvmArgs = RelaunchUtil.findJVMArgs();

        Path javaExecutable = RelaunchUtil.findJavaExecutable();

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
