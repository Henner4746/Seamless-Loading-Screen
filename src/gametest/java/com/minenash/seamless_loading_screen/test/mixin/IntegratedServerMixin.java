package com.minenash.seamless_loading_screen.test.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Leaving a world outside of a test task deadlocks Fabric's client game tests for 26.2: halt() waits for the server
 * thread, which waits for the next test phase. Fabric API for 26.3 ships this same workaround, so it only matters there.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @WrapOperation(method = "halt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;executeBlocking(Ljava/lang/Runnable;)V"))
    private void queueGuestRemoval(IntegratedServer server, Runnable task, Operation<Void> original) {
        if (server.isSameThread()) {
            original.call(server, task);
        } else if (!server.isStopped()) {
            server.execute(task);
        }
    }
}
