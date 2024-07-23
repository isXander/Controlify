package dev.isxander.controlify.driver.steamdeck;

public enum SteamDeckMode {
    GAMING_MODE,
    DESKTOP_MODE,
    NOT_STEAM_DECK;

    public boolean isSteamDeck() {
        return this != NOT_STEAM_DECK;
    }

    // TODO: this doesn't work and reports NOT_STEAM_DECK, on a steam deck...
    public static final SteamDeckMode CURRENT_MODE;
    static {
        boolean isSteamDeck = "1".equals(System.getenv("SteamDeck"));
        boolean isGamingMode = "1".equals(System.getenv("SteamOS"));
        CURRENT_MODE = isSteamDeck ? (isGamingMode ? GAMING_MODE : DESKTOP_MODE) : NOT_STEAM_DECK;
    }
}
