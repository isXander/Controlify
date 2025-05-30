package dev.isxander.splitscreen.mixins.followingame;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.splitscreen.SplitscreenBootstrapper;
import dev.isxander.splitscreen.relauncher.RelaunchQuickPlayFormat;
import dev.isxander.splitscreen.remote.RemotePawnMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.quickplay.QuickPlay;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(QuickPlay.class)
public class QuickPlayMixin {
    @WrapMethod(method = "joinMultiplayerWorld")
    private static void doRelaunchQuickplay(Minecraft minecraft, String string, Operation<Void> original) {
        RelaunchQuickPlayFormat.parse(string)
                .ifLeft(quickPlayFormat -> joinRelaunchedServer(minecraft, quickPlayFormat))
                .ifRight(ip -> original.call(minecraft, ip));
    }

    @Unique
    private static void joinRelaunchedServer(Minecraft minecraft, RelaunchQuickPlayFormat format) {
        RemotePawnMain remotePawn = SplitscreenBootstrapper.getPawn()
                .orElseThrow(() -> new IllegalStateException("Parsed relaunched quickplay format but this is not a pawn."));

        format.nonce().ifPresent(nonce -> {
            try {
                byte[] nonceBytes = Hex.decodeHex(nonce);
                remotePawn.getPawn().setLastLoginNonce(nonceBytes);
            } catch (DecoderException e) {
                throw new IllegalStateException("Could not parse login nonce provided", e);
            }
        });


        var serverData = new ServerData("Relaunched Initial World", format.ip(), ServerData.Type.OTHER);
        var serverAddress = ServerAddress.parseString(format.ip());
        ConnectScreen.startConnecting(new TitleScreen(), minecraft, serverAddress, serverData, true, null);
    }
}
