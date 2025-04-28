package dev.isxander.controlify.splitscreen.host.relaunch;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record LaunchInfo(
        Path javaExecutable,
        List<String> jvmArgs,
        String classpath,
        String mainClass,
        List<String> gameArgs,
        Path workingDirectory
) {
    public List<String> buildCommand() {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable.toString());
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass);
        command.addAll(gameArgs);
        return command;
    }

    public ProcessBuilder buildProcess() {
        return new ProcessBuilder()
                .command(buildCommand())
                .directory(workingDirectory.toFile());
    }
}
