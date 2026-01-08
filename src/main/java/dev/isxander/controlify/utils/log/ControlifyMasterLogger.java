package dev.isxander.controlify.utils.log;

import dev.isxander.controlify.debug.DebugProperties;
import org.jspecify.annotations.Nullable;

public class ControlifyMasterLogger extends AbstractControlifyLogger {
    private final org.slf4j.Logger logger;

    public ControlifyMasterLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void log0(String message, Object[] args, @Nullable Throwable throwable, boolean debug, LogLevel level) {
        super.log0(message, args, throwable, debug, level);
        if (!debug || DebugProperties.DEBUG_LOGGING) {
            if (throwable != null) {
                Object[] newArgs = new Object[args.length + 1];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[args.length] = throwable;
                args = newArgs;
            }

            switch (level) {
                case INFO -> logger.info(message, args);
                case WARN -> logger.warn(message, args);
                case ERROR -> logger.error(message, args);
            }
        }
    }

    @Override
    public ControlifyLogger createSubLogger(String name) {
        return new ControlifySubLoggerImpl(this, name);
    }
}
