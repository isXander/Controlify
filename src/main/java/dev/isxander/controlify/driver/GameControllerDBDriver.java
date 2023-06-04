package dev.isxander.controlify.driver;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GameControllerDBDriver implements NameProviderDriver {
    private static final Map<String, String> GUID_TO_NAME = generateNameMap();

    private final String name;

    public GameControllerDBDriver(String guid) {
        this.name = GUID_TO_NAME.getOrDefault(guid, "Unknown Controller");
    }

    @Override
    public void update() {

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getNameProviderDetails() {
        return "gamecontrollerdb.txt";
    }

    public static boolean isSupported(String guid) {
        return GUID_TO_NAME.containsKey(guid);
    }

    private static Map<String, String> generateNameMap() {
        Resource resource = Minecraft.getInstance().getResourceManager()
                .getResource(Controlify.id("controllers/gamecontrollerdb.txt"))
                .orElseThrow();

        try (BufferedReader reader = resource.openAsReader()) {
            return reader
                    .lines()//.parallel() for some reason this causes deadlock https://stackoverflow.com/questions/34820066/why-does-parallel-stream-with-lambda-in-static-initializer-cause-a-deadlock
                    .filter(line -> !line.startsWith("#") && !line.isBlank())
                    .map(line -> line.split(","))
                    .filter(entry -> entry.length >= 2)
                    .collect(Collectors.toUnmodifiableMap(
                            entry -> entry[0], // guid
                            entry -> entry[1], // name,
                            (a, b) -> a // if there are duplicates, just use the first one
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
