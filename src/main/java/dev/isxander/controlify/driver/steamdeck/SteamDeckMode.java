package dev.isxander.controlify.driver.steamdeck;

public enum SteamDeckMode {
    GAMING_MODE,
    DESKTOP_MODE,
    NOT_STEAM_DECK;

    public boolean isSteamDeck() {
        return this != NOT_STEAM_DECK;
    }
    public boolean isGamingMode() {
        return this == GAMING_MODE;
    }
}
