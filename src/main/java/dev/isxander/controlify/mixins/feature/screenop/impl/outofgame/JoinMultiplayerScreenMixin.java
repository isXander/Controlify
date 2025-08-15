package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.JoinMultiplayerScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin implements ScreenProcessorProvider {
    @Shadow protected ServerSelectionList serverSelectionList;

    @Unique
    private Button backButton;
    @Unique
    private Button directConnectButton;
    @Unique
    private Button addServerButton;

    @Unique
    private final JoinMultiplayerScreenProcessor processor = new JoinMultiplayerScreenProcessor(
            (JoinMultiplayerScreen) (Object) this,
            () -> this.serverSelectionList,
            () -> this.backButton,
            () -> this.directConnectButton,
            () -> this.addServerButton
    );

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Definition(id = "GUI_BACK", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;")
    @Expression("builder(GUI_BACK, ?).?(?).build()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button captureBackButton(Button button) {
        return this.backButton = button;
    }

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Expression("builder(translatable('selectServer.direct'), ?).?(?).build()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button captureDirectConnectButton(Button button) {
        return this.directConnectButton = button;
    }

    @Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
    @Expression("builder(translatable('selectServer.add'), ?).?(?).build()")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Button captureAddServerButton(Button button) {
        return this.addServerButton = button;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
