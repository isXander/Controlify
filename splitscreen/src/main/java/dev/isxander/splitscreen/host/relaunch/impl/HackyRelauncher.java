package dev.isxander.splitscreen.host.relaunch.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.splitscreen.host.relaunch.LaunchInfo;
import dev.isxander.splitscreen.relauncher.RelaunchException;
import dev.isxander.splitscreen.host.relaunch.RelaunchUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
        List<String> gameArgs = splitGameArgs(joinedGameArgs);

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


    // Converts '--flag some value --otherflag some other value' to '["--flag", "some value", "--otherflag", "some other value"]'
    private static final Pattern FLAG_PATTERN = Pattern.compile("(--\\S+)\\s+(.+?)(?=(?:\\s+--\\S+)|$)");
    private static List<String> splitGameArgs(String args) {
        return FLAG_PATTERN.matcher(args)
                .results()
                .flatMap(m -> Stream.of(
                        m.group(1),
                        m.group(2).trim()
                ))
                .toList();
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
