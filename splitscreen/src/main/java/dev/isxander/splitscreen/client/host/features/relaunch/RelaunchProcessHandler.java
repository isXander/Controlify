package dev.isxander.splitscreen.client.host.features.relaunch;

import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.host.RemoteSplitscreenPawn;
import dev.isxander.splitscreen.client.host.SplitscreenController;
import dev.isxander.splitscreen.client.host.features.relaunch.impl.FabricLoaderRelauncher;
import dev.isxander.splitscreen.client.host.util.LANUtil;
import dev.isxander.splitscreen.client.ipc.IPCMethod;
import dev.isxander.splitscreen.client.host.features.relaunch.impl.HackyRelauncher;
import dev.isxander.splitscreen.client.host.features.relaunch.impl.PrismRelauncher;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchException;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchQuickPlayFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RelaunchProcessHandler {

    private final Process process;
    private final int pawnIndex;
    private final Logger logger;

    private @Nullable RemoteSplitscreenPawn pawn;

    public static RelaunchProcessHandler createProcess(Minecraft minecraft, InputMethod inputMethod, SplitscreenController splitscreenController, int pawnIndex, IPCMethod ipcMethod) {
        boolean isDevLaunchInjector = FabricLoader.getInstance().isDevelopmentEnvironment();
        LaunchInfo launchInfo = PrismRelauncher.isPrism() ? PrismRelauncher.getLaunchInfo() : !isDevLaunchInjector ? FabricLoaderRelauncher.getLaunchInfo() : HackyRelauncher.getLaunchInfo();

        Path argFile;
        try {
            argFile = launchInfo.workingDirectory().relativize(Files.createTempFile(launchInfo.workingDirectory(), "pawn-args", ".txt"));
        } catch (IOException e) {
            throw new RelaunchException("Failed to create arg file", e);
        }

        // append own JVM args to pass to the new process
        launchInfo.jvmArgs().add(RelaunchArguments.RELAUNCHED.asArgument(true));
        launchInfo.jvmArgs().add(RelaunchArguments.INPUT_METHOD.asArgument(inputMethod));
        launchInfo.jvmArgs().add(RelaunchArguments.PAWN_INDEX.asArgument(pawnIndex));
        launchInfo.jvmArgs().add(RelaunchArguments.HOST_UUID.asArgument(minecraft.getUser().getProfileId()));
        launchInfo.jvmArgs().add(RelaunchArguments.ARGFILE_PATH.asArgument(argFile.toString()));
        switch (ipcMethod) {
            case IPCMethod.TCP(int port) -> launchInfo.jvmArgs().add(RelaunchArguments.IPC_TCP_PORT.asArgument(port));
            case IPCMethod.Unix(String socket) -> launchInfo.jvmArgs().add(RelaunchArguments.IPC_SOCKET_PATH.asArgument(socket));
            default -> throw new UnsupportedOperationException("Unrecognized IPC method: " + ipcMethod);
        }

        String username = minecraft.getUser().getName();
        String pawnUsername = username + "." + pawnIndex;
        launchInfo.jvmArgs().add(RelaunchArguments.USERNAME.asArgument(pawnUsername));

        LANUtil.getOrPublishLANServer()
                .or(() -> Optional.ofNullable(minecraft.getCurrentServer())
                        .map(data -> ServerAddress.parseString(data.ip)))
                .ifPresent(address -> {
                    var quickPlayFormat = RelaunchQuickPlayFormat.asString(
                            address.toString(),
                            Hex.encodeHexString(splitscreenController.getLocalPawn().getLastLoginNonce())
                    );
                    launchInfo.jvmArgs().add(RelaunchArguments.LAN_GAME.asArgument(quickPlayFormat));
                });

        return new RelaunchProcessHandler(launchInfo, pawnIndex, argFile);
    }

    private RelaunchProcessHandler(LaunchInfo launchInfo, int pawnIndex, Path argfilePath) {
        this.pawnIndex = pawnIndex;
        this.logger = LoggerFactory.getLogger("RelaunchProcessHandler#" + pawnIndex);
        this.process = handleProcess(launchInfo, argfilePath);
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public CompletableFuture<RelaunchProcessHandler> onExit() {
        return process.onExit().thenApply(proc -> this);
    }

    private Process startProcess(LaunchInfo launchInfo, Path argfilePath) {
        try {
            String argFileContent = launchInfo.buildArgfile();
            Files.writeString(argfilePath, argFileContent);

            ProcessBuilder processBuilder = launchInfo.buildProcessWithArgfile(argfilePath)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .redirectInput(ProcessBuilder.Redirect.INHERIT);

            try {
                return processBuilder.start();
            } catch (IOException e) {
                throw new RelaunchException("Failed to start new pawn process", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Process handleProcess(LaunchInfo launchInfo, Path argfilePath) {
        final Thread[] readerThreads = new Thread[2];
        Process process = startProcess(launchInfo, argfilePath);

        readerThreads[0] = Thread.ofVirtual().name("stdout-reader")
                .start(pipeStream(process.getInputStream(), System.out));
        readerThreads[1] = Thread.ofVirtual().name("stderr-reader")
                .start(pipeStream(process.getErrorStream(), System.err));

        logger.info("Pawn #{} started with PID {}", pawnIndex, process.pid());

        CompletableFuture<Process> onExit = process.onExit();

        onExit.thenAcceptAsync(exitedProcess -> {
            logger.info("Pawn #{} exited with code {}", pawnIndex, exitedProcess.exitValue());

            try {
                for (Thread thread : readerThreads) {
                    thread.join(1000);
                }
                logger.info("All reader threads joined successfully for Pawn #{}", pawnIndex);
            } catch (InterruptedException e) {
                logger.error("Async callback interrupted while joining reader threads for Pawn #{}", pawnIndex, e);
                Thread.currentThread().interrupt();
            }
        });

        onExit.exceptionally(throwable -> {
            logger.error("An exception occurred in the async completion stage", throwable);
            return null;
        });

        return process;
    }

    public void setPawn(@Nullable RemoteSplitscreenPawn pawn) {
        if (this.pawn != null && this.pawn != pawn) {
            throw new IllegalStateException("Pawn already set to " + this.pawn);
        }
        this.pawn = pawn;
    }

    public @Nullable RemoteSplitscreenPawn getPawn() {
        return pawn;
    }

    private Runnable pipeStream(InputStream stream, PrintStream output) {
        return () -> {
            String prefix = "[Pawn #" + pawnIndex + "] ";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.println(prefix + line);
                }
            } catch (IOException e) {
                logger.error("Failed to read from stream", e);
            }
        };
    }
}
