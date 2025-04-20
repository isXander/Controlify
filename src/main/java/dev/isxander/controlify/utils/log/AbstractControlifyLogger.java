package dev.isxander.controlify.utils.log;

import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractControlifyLogger implements ControlifyLogger {
    private final Queue<LogMessage> logMessages = new ConcurrentLinkedQueue<>();

    @Override
    public void log(String message) {
        log0(message, new Object[0], null, false, LogLevel.INFO);
    }

    @Override
    public void log(String message, Object... args) {
        log0(message, args, null, false, LogLevel.INFO);
    }

    @Override
    public void log(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, false, LogLevel.INFO);
    }

    @Override
    public void log(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, false, LogLevel.INFO);
    }

    @Override
    public void warn(String message) {
        log0(message, new Object[0], null, false, LogLevel.WARN);
    }

    @Override
    public void warn(String message, Object... args) {
        log0(message, args, null, false, LogLevel.WARN);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, false, LogLevel.WARN);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, false, LogLevel.WARN);
    }

    @Override
    public void error(String message) {
        log0(message, new Object[0], null, false, LogLevel.ERROR);
    }

    @Override
    public void error(String message, Object... args) {
        log0(message, args, null, false, LogLevel.ERROR);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, false, LogLevel.ERROR);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, false, LogLevel.ERROR);
    }

    @Override
    public void debugLog(String message) {
        log0(message, new Object[0], null, true, LogLevel.INFO);
    }

    @Override
    public void debugLog(String message, Object... args) {
        log0(message, args, null, true, LogLevel.INFO);
    }

    @Override
    public void debugLog(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, true, LogLevel.INFO);
    }

    @Override
    public void debugLog(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, true, LogLevel.INFO);
    }

    @Override
    public void debugWarn(String message) {
        log0(message, new Object[0], null, true, LogLevel.WARN);
    }

    @Override
    public void debugWarn(String message, Object... args) {
        log0(message, args, null, true, LogLevel.WARN);
    }

    @Override
    public void debugWarn(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, true, LogLevel.WARN);
    }

    @Override
    public void debugWarn(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, true, LogLevel.WARN);
    }

    @Override
    public void debugError(String message) {
        log0(message, new Object[0], null, true, LogLevel.ERROR);
    }

    @Override
    public void debugError(String message, Object... args) {
        log0(message, args, null, true, LogLevel.ERROR);
    }

    @Override
    public void debugError(String message, Throwable throwable) {
        log0(message, new Object[0], throwable, true, LogLevel.ERROR);
    }

    @Override
    public void debugError(String message, Throwable throwable, Object... args) {
        log0(message, args, throwable, true, LogLevel.ERROR);
    }

    @Override
    public void crashReport(CrashReport report) {
        debugError(report.getFriendlyReport(ReportType.CRASH));
    }

    @Override
    public void validateIsTrue(boolean condition, String message) {
        if (!condition) {
            debugError("Validation failed: " + message);
            throw new AssertionError("Validation failed: " + message);
        }
    }

    protected void log0(String message, Object[] args, @Nullable Throwable throwable, boolean debug, LogLevel level) {
        this.logMessages.add(new LogMessage(message, args, throwable, debug, level));
    }

    @Override
    public String export() {
        return logMessages.stream()
                .reduce(
                        new StringBuilder(),
                        (b, m) -> m.append(b),
                        StringBuilder::append
                ).toString();
    }
}
