package dev.isxander.splitscreen.client.host.features.relaunch;

import dev.isxander.splitscreen.client.features.relaunch.RelaunchException;
import dev.isxander.controlify.utils.Platform;
import org.jetbrains.annotations.NotNull;

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

    // https://github.com/FabricMC/fabric-loom/blob/b37c4d3474fccd30f69beb25a20cc84da94f0574/src/main/java/net/fabricmc/loom/task/AbstractRunTask.java#L173-L198
    public static String quoteArg(String arg) {
        final String specials = " #'\"\n\r\t\f";

        if (!containsAnyChar(arg, specials)) {
            return arg;
        }

        final StringBuilder sb = new StringBuilder(arg.length() * 2);

        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);

            switch (c) {
                case ' ', '#', '\'' -> sb.append('"').append(c).append('"');
                case '"' -> sb.append("\"\\\"\"");
                case '\n' -> sb.append("\"\\n\"");
                case '\r' -> sb.append("\"\\r\"");
                case '\t' -> sb.append("\"\\t\"");
                case '\f' -> sb.append("\"\\f\"");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    private static boolean containsAnyChar(@NotNull String value, @NotNull String chars) {
        return chars.length() > value.length()
                ? containsAnyChar(value, chars, 0, value.length())
                : containsAnyChar(chars, value, 0, chars.length());
    }

    private static boolean containsAnyChar(final @NotNull String value, final @NotNull String chars, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (chars.indexOf(value.charAt(i)) >= 0) {
                return true;
            }
        }

        return false;
    }
}
