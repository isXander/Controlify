package dev.isxander.controlify.splitscreen.host.relaunch.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.splitscreen.host.relaunch.LaunchInfo;
import dev.isxander.controlify.splitscreen.relauncher.RelaunchException;
import dev.isxander.controlify.splitscreen.host.relaunch.RelaunchUtil;

import java.nio.file.Path;
import java.util.List;

public class HackyRelauncher {
    private static LaunchInfo launchInfo = null;

    public static LaunchInfo getLaunchInfo() {
        if (launchInfo == null) {
            launchInfo = createLaunchInfo();
        }
        return launchInfo;
    }

    private static LaunchInfo createLaunchInfo() {
        Path javaExecutable = RelaunchUtil.findJavaExecutable();
        String mainClass = findEntrypointMainClassFromStacktrace();

        String programArgs = System.getProperty("sun.java.command");

        if (programArgs == null) {
            throw new RelaunchException("Could not find program arguments");
        }
        if (!programArgs.startsWith(mainClass)) {
            throw new RelaunchException("Program arguments do not start with main class");
        }

        // +1 to skip the space after the main class
        String joinedGameArgs = programArgs.substring(mainClass.length()).trim();
        List<String> gameArgs = List.of(joinedGameArgs.split(" "));

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

    private static String findEntrypointMainClassFromStacktrace() {
        // make sure we're on the client thread to find the main entrypoint, not a thread's entrypoint
        RenderSystem.assertOnRenderThread();

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTrace = currentThread.getStackTrace();

        StackTraceElement bottomFrame = stackTrace[stackTrace.length - 1];
        if (!bottomFrame.getMethodName().equals("main")) {
            throw new RelaunchException("Could not find entrypoint main class: The bottom stack frame is not main.");
        }
        return bottomFrame.getClassName();
    }
}
