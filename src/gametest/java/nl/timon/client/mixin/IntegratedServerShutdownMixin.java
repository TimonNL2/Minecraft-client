package nl.timon.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Test fixture only; never packaged in the mod. Fabric's client test phaser parks
 * the server between ticks. Vanilla's blocking LAN-player cleanup in halt then
 * deadlocks the client before Fabric can pump its shutdown phases. Queue that
 * cleanup in our private, unpublished, single-player test server instead.
 * World saving and the normal server halt still run; assertions run beforehand.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerShutdownMixin {
    @WrapOperation(method = "halt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;executeBlocking(Ljava/lang/Runnable;)V"))
    private void queueTestShutdown(IntegratedServer server, Runnable task, Operation<Void> original) {
        if (server.isPublished()) throw new IllegalStateException("Test fixture must never be a LAN server");
        server.execute(task);
    }
}
