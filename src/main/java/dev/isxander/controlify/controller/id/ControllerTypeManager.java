package dev.isxander.controlify.controller.id;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.JsonTreeParser;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.quiltmc.parsers.json.JsonReader;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ControllerTypeManager implements SimpleControlifyReloadListener<ControllerTypeManager.Preparations> {

    private Map<HIDIdentifier, ControllerType> typeMap = new HashMap<>();

    public static final ResourceLocation ID = CUtil.rl("controller_type");

    private static final Codec<ControllerTypeEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(HIDIdentifier.LIST_CODEC)
                    .comapFlatMap(list -> list.isEmpty() ? DataResult.error(() -> "At least one HID must be present") : DataResult.success(list), list -> list)
                    .fieldOf("hids")
                    .forGetter(ControllerTypeEntry::hid),
            ControllerType.CODEC.forGetter(ControllerTypeEntry::type)
    ).apply(instance, ControllerTypeEntry::new));

    public ControllerType getControllerType(HIDIdentifier hid) {
        return typeMap.getOrDefault(hid, ControllerType.DEFAULT);
    }

    public Map<HIDIdentifier, ControllerType> getTypeMap() {
        return typeMap;
    }

    @Override
    public CompletableFuture<Preparations> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> manager.getResourceStack(CUtil.rl("controllers/controller_identification.json5")), executor)
                .thenCompose(resources -> {
                    List<CompletableFuture<List<Map.Entry<HIDIdentifier, ControllerType>>>> futures = new ArrayList<>();
                    for (Resource resource : resources) {
                        futures.add(CompletableFuture.supplyAsync(() -> readIdentificationResource(resource), executor));
                    }

                    return Util.sequence(futures)
                            .thenApply(listOfEntries -> listOfEntries.stream()
                                    .flatMap(List::stream)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)));

                })
                .thenApply(Preparations::new);
    }

    private List<Map.Entry<HIDIdentifier, ControllerType>> readIdentificationResource(Resource resource) {
        Map<HIDIdentifier, ControllerType> typeMap = new HashMap<>();

        try (BufferedReader resourceReader = resource.openAsReader()) {
            var reader = JsonReader.json5(resourceReader);
            JsonElement json = JsonTreeParser.parse(reader);

            ENTRY_CODEC.listOf().parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(CUtil.LOGGER::error)
                    .ifPresent(entries -> {
                        for (var entry : entries) {
                            for (var hid : entry.hid()) {
                                typeMap.put(hid, entry.type());
                            }
                        }
                    });
        } catch (Exception e) {
            CUtil.LOGGER.error("Failed to read controller identification database!", e);
        }

        return typeMap.entrySet().stream().toList();
    }

    @Override
    public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.typeMap = data.typeMap();
            triggerFullTypeReload();
        }, executor);
    }

    public void triggerFullTypeReload() {
        Optional<ControllerManager> controllerManagerOpt = Controlify.instance().getControllerManager();
        if (controllerManagerOpt.isPresent()) {
            ControllerManager controllerManager = controllerManagerOpt.get();

            for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
                reloadTypeForController(controllerManager, controller);
            }
        }
    }

    public void reloadTypeForController(ControllerManager controllerManager, ControllerEntity controller) {
        Optional<HIDDevice> hidOpt = controller.info().hid();
        if (hidOpt.isEmpty()) return;

        HIDDevice hid = hidOpt.get();

        ControllerType newType = this.getControllerType(hid.asIdentifier());
        ControllerType oldType = controller.info().type();

        // re-initialise the controller if its type has changed
        if (!newType.equals(oldType)) {
            controllerManager.reinitController(
                    controller,
                    new ControllerHIDService.ControllerHIDInfo(
                            newType,
                            controller.info().hid()
                    )
            );
        }
    }

    @Override
    public ResourceLocation getReloadId() {
        return ID;
    }


    public record Preparations(Map<HIDIdentifier, ControllerType> typeMap) {}

    private record ControllerTypeEntry(List<HIDIdentifier> hid, ControllerType type) {}
}
