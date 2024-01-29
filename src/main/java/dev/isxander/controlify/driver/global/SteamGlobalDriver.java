package dev.isxander.controlify.driver.global;

import dev.isxander.controlify.utils.CUtil;

public class SteamGlobalDriver implements OnScreenKeyboardDriver {
//    public static final boolean IS_STEAM_DECK = "1".equals(System.getenv("STEAM_DECK"));
    public static final boolean IS_STEAM_DECK = true;
    private boolean keyboardShown = false;

    @Override
    public void openOnScreenKeyboard(int obstructionX, int obstructionY, int obstructionWidth, int obstructionHeight) {
        // Mode=0 makes enter hide keyboard
        // https://partner.steamgames.com/doc/api/ISteamUtils#ShowFloatingGamepadTextInput
        CUtil.openUri("steam://open/keyboard?XPosition=%s&YPosition=%s&Width=%s&Height=%s&Mode=0"
                .formatted(obstructionX, obstructionY, obstructionWidth, obstructionHeight));
        keyboardShown = true;
    }

    @Override
    public void closeOnScreenKeyboard() {
        CUtil.openUri("steam://close/keyboard");
        keyboardShown = false;
    }

    @Override
    public boolean isKeyboardShown() {
        return keyboardShown;
    }

    @Override
    public String keyboardDriverDetails() {
        return "Steam";
    }

    @Override
    public boolean isOnScreenKeyboardSupported() {
        return true;
    }
}
