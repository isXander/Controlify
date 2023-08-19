package dev.isxander.controlify.server;

import java.util.Map;

public enum ServerPolicies {
    REACH_AROUND("reachAround"),
    DISABLE_FLY_DRIFTING("disableFlyDrifting");

    private static final Map<String, ServerPolicies> BY_ID = Map.of(
            REACH_AROUND.getId(), REACH_AROUND,
            DISABLE_FLY_DRIFTING.getId(), DISABLE_FLY_DRIFTING
    );

    private final String id;
    private ServerPolicy value;

    ServerPolicies(String id) {
        this.id = id;
        this.value = ServerPolicy.UNSET;
    }

    public ServerPolicy get() {
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
