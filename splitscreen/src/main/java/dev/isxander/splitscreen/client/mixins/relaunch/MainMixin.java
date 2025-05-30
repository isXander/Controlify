package dev.isxander.splitscreen.client.mixins.relaunch;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.properties.PropertyMap;
import dev.isxander.splitscreen.client.features.relaunch.RelaunchArguments;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.Main;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Main.class)
public class MainMixin {
    /**
     * When using relaunch, we can't log into the same account twice, take the username from the relaunch
     * arguments and overwrite the user nonce with that.
     * @param originalData the original user nonce, duplicated from the controller client
     * @return the modified user nonce
     */
    @ModifyExpressionValue(method = "main", at = @At(value = "NEW", target = "net/minecraft/client/main/GameConfig$UserData"))
    private static GameConfig.UserData modifyUserData(GameConfig.UserData originalData) {
        return RelaunchArguments.USERNAME.get()
                .map(username -> new GameConfig.UserData(
                        new User(
                                username,
                                UUIDUtil.createOfflinePlayerUUID(username),
                                "",
                                Optional.empty(),
                                Optional.empty(),
                                User.Type.LEGACY
                        ),
                        new PropertyMap(),
                        new PropertyMap(),
                        originalData.proxy
                ))
                .orElse(originalData);
    }

    /**
     * It's possible we relaunched directly from in-game. In this case,
     * instead of reinventing the wheel, just pipe our LAN server into quickplay.
     * @param originalData the original quickplay nonce, duplicated from the controller client
     * @return the modified quickplay nonce
     */
    @ModifyExpressionValue(method = "main", at = @At(value = "NEW", target = "net/minecraft/client/main/GameConfig$QuickPlayData"))
    private static GameConfig.QuickPlayData modifyQuickPlayData(GameConfig.QuickPlayData originalData) {
        return RelaunchArguments.LAN_GAME.get()
                .map(lanIp -> new GameConfig.QuickPlayData(
                    null, // path to world
                        null, // singleplayer world name
                        lanIp, // multiplayer ip
                        null // realm id
                ))
                .orElse(originalData);
    }
}
