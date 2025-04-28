package dev.isxander.controlify.splitscreen.host.relaunch;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.host.util.LANUtil;
import dev.isxander.controlify.splitscreen.ipc.IPCMethod;
import dev.isxander.controlify.splitscreen.host.relaunch.impl.HackyRelauncher;
import dev.isxander.controlify.splitscreen.host.relaunch.impl.PrismRelauncher;
import dev.isxander.controlify.splitscreen.relauncher.RelaunchArguments;
import dev.isxander.controlify.splitscreen.relauncher.RelaunchException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class RelaunchProcessHandler {

    private final Process process;
    private final int pawnIndex;
    private final Logger logger;

    public static RelaunchProcessHandler createProcess(Minecraft minecraft, ControllerUID controller, int pawnIndex, IPCMethod ipcMethod) {
        LaunchInfo launchInfo = PrismRelauncher.isPrism() ? PrismRelauncher.getLaunchInfo() : HackyRelauncher.getLaunchInfo();

        // append own JVM args to pass to the new process
        launchInfo.jvmArgs().add(RelaunchArguments.RELAUNCHED.asArgument(true));
        launchInfo.jvmArgs().add(RelaunchArguments.CONTROLLER.asArgument(controller));
        launchInfo.jvmArgs().add(RelaunchArguments.PAWN_INDEX.asArgument(pawnIndex));
        switch (ipcMethod) {
            case IPCMethod.TCP(int port) -> launchInfo.jvmArgs().add(RelaunchArguments.IPC_TCP_PORT.asArgument(port));
            case IPCMethod.Unix(String socket) -> throw new UnsupportedOperationException("Unix sockets are not supported yet");
        }

        String username = minecraft.getUser().getName();
        String pawnUsername = username + pawnIndex;
        launchInfo.jvmArgs().add(RelaunchArguments.USERNAME.asArgument(pawnUsername));

        LANUtil.getOrPublishLANServer().ifPresent(address ->
                launchInfo.jvmArgs().add(RelaunchArguments.LAN_GAME.asArgument(address.toString())));

        return new RelaunchProcessHandler(launchInfo, pawnIndex);
    }

    public RelaunchProcessHandler(LaunchInfo launchInfo, int pawnIndex) {
        this.pawnIndex = pawnIndex;
        this.logger = LoggerFactory.getLogger("RelaunchProcessHandler#" + pawnIndex);
        this.process = startProcess(launchInfo);
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public CompletableFuture<RelaunchProcessHandler> onExit() {
        return process.onExit().thenApply(proc -> this);
    }

    private Process startProcess(LaunchInfo launchInfo) {
        ProcessBuilder processBuilder = launchInfo.buildProcess()
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectInput(ProcessBuilder.Redirect.INHERIT);

        final Thread[] readerThreads = new Thread[2];
        try {
            Process process = processBuilder.start();

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
        } catch (IOException e) {
            throw new RelaunchException("Failed to start new pawn process", e);
        }
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
