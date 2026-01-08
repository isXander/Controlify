package dev.isxander.controlify.utils.log;

import org.jspecify.annotations.Nullable;

public class ControlifySubLoggerImpl extends AbstractControlifyLogger implements ControlifySubLogger {
    private final AbstractControlifyLogger parent;
    private final String name;

    public ControlifySubLoggerImpl(AbstractControlifyLogger parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    protected void log0(String message, Object[] args, @Nullable Throwable throwable, boolean debug, LogLevel level) {
        super.log0(withName(message, name), args, throwable, debug, level);
        parent.log0(withName(message, name), args, throwable, debug, level);
    }

    @Override
    public ControlifyLogger createSubLogger(String name) {
        return new ControlifySubLoggerImpl(this, name);
    }

    private String withName(String message, String name) {
        return String.format("[%s] %s", name, message);
    }
}
