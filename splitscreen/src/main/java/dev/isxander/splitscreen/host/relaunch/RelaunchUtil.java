package dev.isxander.splitscreen.host.relaunch;

import dev.isxander.splitscreen.relauncher.RelaunchException;
import dev.isxander.controlify.utils.Platform;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelaunchUtil {

    public static Path findJavaExecutable() {
        return findJavaExecutableFromProcessHandle()
                .or(RelaunchUtil::findJavaExecutableFromSystemProperty)
                .orElseThrow(() -> new RelaunchException("Could not find Java executable"));
    }

    private static Optional<Path> findJavaExecutableFromProcessHandle() {
        return ProcessHandle.current().info().command()
                .map(Path::of)
                .filter(Path::isAbsolute)
                .filter(Files::isExecutable);
    }

    private static Optional<Path> findJavaExecutableFromSystemProperty() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isEmpty()) {
            return Optional.empty();
        }

        Path jrePath = Path.of(javaHome);
        boolean isWindows = Platform.current() == Platform.WINDOWS;
        boolean consoleless = System.console() == null;

        String executableName = isWindows ? (consoleless ? "javaw.exe" : "java.exe") : "java";
        Path executablePath = jrePath.resolve(executableName);

        if (Files.exists(executablePath) && Files.isExecutable(executablePath)) {
            return Optional.of(executablePath.toAbsolutePath());
        }

        return Optional.empty();
    }

    public static List<String> findJVMArgs() {
        return new ArrayList<>(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    public static String findClasspath() {
        return ManagementFactory.getRuntimeMXBean().getClassPath();
    }

    public static Path findWorkingDirectory() {
        return Path.of(System.getProperty("user.dir"));
    }
}
