package dev.isxander.controlify.driver.steamdeck;

import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.deckapi.api.SteamDeck;
import dev.isxander.deckapi.api.SteamDeckException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public final class SteamDeckUtil {
    private static @Nullable SteamDeck deckInstance;
    private static boolean triedToLoad = false;

    public static final boolean IS_STEAM_DECK = isHardwareSteamDeck();
    public static final SteamDeckMode DECK_MODE = getSteamDeckMode();
    // flatpak sets env variable 'container' when containerised
    // https://stackoverflow.com/a/75284996
    public static final boolean IS_SANDBOXED = "1".equals(System.getenv("container"));

    public static final ResourceLocation STEAM_DECK_NAMESPACE = CUtil.rl("steam_deck");

    public static Optional<SteamDeck> getDeckInstance() {
        if (triedToLoad) {
            return Optional.ofNullable(deckInstance);
        }
        triedToLoad = true;

        if (!DECK_MODE.isGamingMode()) {
            CUtil.LOGGER.warn("Device is not a Steam Deck or not in gaming mode, skipping Steam Deck driver initialization.");
            return Optional.empty();
        }

        try {
            String url = DebugProperties.STEAM_DECK_CUSTOM_CEF_URL;
            if (url == null) url = SteamDeck.DEFAULT_URL;

            deckInstance = SteamDeck.create(url);
        } catch (SteamDeckException e) {
            CUtil.LOGGER.error("Failed to create SteamDeck instance", e);
            deckInstance = null;
        }

        return Optional.ofNullable(deckInstance);
    }

    private static boolean isHardwareSteamDeck() {
        // even if "Linux" isn't a defacto way to check for all linux distros, it's the value returned on a steam deck
        boolean isLinux = "Linux".equals(System.getProperty("os.name"));
        if (!isLinux) return false;

        String kernelVersion = System.getProperty("os.version");
        // steam decks use a special kernel from valve
        // this check is not used because i believe the board information is more reliable
        // as i'm not sure if it's common for people to use other kernels on steam decks
        boolean valveKernel = kernelVersion.contains("valve");

        String boardVendor = readFile("/sys/class/dmi/id/board_vendor");
        if (boardVendor == null) return false;

        String boardName = readFile("/sys/class/dmi/id/board_name");
        if (boardName == null) return false;

        var validBoardNames = Stream.of(
                "Jupiter", // LCD
                "Galileo"  // OLED
        );

        // Jupiter is the codename for the steam deck
        return boardVendor.contains("Valve") && validBoardNames.anyMatch(boardName::contains);
    }

    private static SteamDeckMode getSteamDeckMode() {
        if (IS_STEAM_DECK) {
            String steamDeck = System.getenv("SteamDeck");
            // only set if in gaming mode
            if (steamDeck != null && steamDeck.equals("1")) {
                return SteamDeckMode.GAMING_MODE;
            } else {
                return SteamDeckMode.DESKTOP_MODE;
            }
        } else {
            return SteamDeckMode.NOT_STEAM_DECK;
        }
    }

    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
    }
}
