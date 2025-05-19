package dev.isxander.controlify.splitscreen.mixins.framethrottle;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.remote.RemotePawnMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(FramerateLimitTracker.class)
public class FramerateLimitTrackerMixin {
    /**
     * If this window is hidden by the host engine, it will tell us to throttle.
     * In this case where we cannot see the window whatsoever, we can limit its fps to the lowest we can.
     * @param original the original method to call
     * @return the framerate limit to use
     */
    @WrapMethod(method = "getFramerateLimit")
    private int throttleFramerateIfHidden(Operation<Integer> original) {
        if (shouldSplitscreenThrottle()) {
            return 1;
        }

        return original.call();
    }

    /**
     * If this window is hidden by the host engine, it will tell us to throttle.
     * In this case where we cannot see the window whatsoever, we can limit its fps to the lowest we can.
     */
    @WrapMethod(method = "isHeavilyThrottled")
    private boolean sayHeavilyThrottledIfHidden(Operation<Boolean> original) {
        if (shouldSplitscreenThrottle()) {
            return true;
        }

        return original.call();
    }

    @Unique
    private boolean shouldSplitscreenThrottle() {
        return SplitscreenBootstrapper.getPawn()
                .map(RemotePawnMain::getSplitscreenEngine)
                .flatMap(engine -> engine instanceof ReparentingRemoteSplitscreenEngine reparentingEngine ? Optional.of(reparentingEngine) : Optional.empty())
                .map(ReparentingRemoteSplitscreenEngine::shouldThrottleFps)
                .orElse(false);
    }
}
