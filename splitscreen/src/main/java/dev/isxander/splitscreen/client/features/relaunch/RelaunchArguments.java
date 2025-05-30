package dev.isxander.splitscreen.client.features.relaunch;

import dev.isxander.controlify.controller.ControllerUID;

import java.util.UUID;

public final class RelaunchArguments {
    public static final RelaunchArgument<Boolean> RELAUNCHED = RelaunchArgument.bool("controlify.splitscreen.relaunched");
    public static final RelaunchArgument<ControllerUID> CONTROLLER = RelaunchArgument.controller("controlify.splitscreen.controller");
    public static final RelaunchArgument<Integer> PAWN_INDEX = RelaunchArgument.integer("controlify.splitscreen.pawn_index");
    public static final RelaunchArgument<Integer> IPC_TCP_PORT = RelaunchArgument.integer("controlify.splitscreen.ipc.tcp_port");
    public static final RelaunchArgument<String> IPC_SOCKET_PATH = RelaunchArgument.string("controlify.splitscreen.ipc.socket_path");
    public static final RelaunchArgument<String> USERNAME = RelaunchArgument.string("controlify.splitscreen.username");
    public static final RelaunchArgument<String> LAN_GAME = RelaunchArgument.string("controlify.splitscreen.lan_game");
    public static final RelaunchArgument<UUID> HOST_UUID = RelaunchArgument.uuid("controlify.splitscreen.host_uuid");
    public static final RelaunchArgument<String> ARGFILE_PATH = RelaunchArgument.string("controlify.splitscreen.argfile_path");

    private RelaunchArguments() {}
}
