package dev.isxander.controlify.server;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ServerPolicies {
    REACH_AROUND("reachAround", true),
    DISABLE_FLY_DRIFTING("disableFlyDrifting", false),
    KEYBOARD_LIKE_MOVEMENT("keyboardLikeMovement", false);

    private static final Map<String, ServerPolicies> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(ServerPolicies::getId, e -> e));

    private final String id;
    private ServerPolicy value;
    private final boolean unsetValue;

    ServerPolicies(String id, boolean unsetValue) {
        this.id = id;
        this.value = ServerPolicy.UNSET;
        this.unsetValue = unsetValue;
    }

    public boolean get() {
        return switch (value) {
            case ALLOWED -> true;
            case DISALLOWED -> false;
            case UNSET -> unsetValue;
        };
    }

    public boolean getUnsetValue() {
        return unsetValue;
    }

    public boolean isUnset() {
        return value == ServerPolicy.UNSET;
    }

    public ServerPolicy getPolicy() {
        return value;
    }

    public void set(ServerPolicy value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public static ServerPolicies getById(String id) {
        return BY_ID.get(id);
    }

    public static void unsetAll() {
        for (ServerPolicies policy : values()) {
            policy.set(ServerPolicy.UNSET);
        }
    }
}
