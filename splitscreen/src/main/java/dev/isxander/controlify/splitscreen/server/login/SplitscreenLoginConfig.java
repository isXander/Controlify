package dev.isxander.controlify.splitscreen.server.login;

public final class SplitscreenLoginConfig {
    private SplitscreenLoginConfig() {}

    /**
     * Amount of sub-players that a server allows to connect from a single account.
     * This is the maximum amount of sub-players that can be connected to a single account.
     */
    public static final int MAX_CLIENTS = 3; // 4 player splitscreen

    /**
     * If the sub-players can pick any username they like, e.g. 'Notch', when false, it will be OriginalUsername1/2/3
     * This is highly discouraged to enable on public servers, as sub-players could impersonate other players.
     */
    public static final boolean ALLOW_ANY_USERNAME = false;

    /**
     * Allow splitscreen logins after the main player has logged in and is already playing.
     * This increases risk because at login the client guarantees the amount of sub-players it will use,
     * but if the main player is already in the game, the host client does not ensure that the amount of sub-players
     * is what it expects.
     */
    public static final boolean ALLOW_LATE_LOGINS = true;
}
