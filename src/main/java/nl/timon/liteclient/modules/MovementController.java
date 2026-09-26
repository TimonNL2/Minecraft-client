package nl.timon.liteclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;

/** Changes only the local player's voluntary movement, retaining normal collisions. */
public final class MovementController {
    private MovementController() {}

    public static Vec3 adjust(Entity entity, MoverType type, Vec3 original) {
        LiteClient client = LiteClient.get();
        Minecraft mc = Minecraft.getInstance();
        if (client == null || entity != mc.player || !mc.isSameThread() || !client.inWorld() || type != MoverType.SELF) {
            return original;
        }

        LocalPlayer player = mc.player;
        if (player.isPassenger() || player.isInLiquid() || player.isSleeping()) return original;

        boolean flight = client.isEnabled(ModuleId.FLIGHT);
        boolean elytra = client.isEnabled(ModuleId.ELYTRA_FLY) && canControlGlider(player);
        if (!flight && !elytra) return original;

        double speed = boundedSpeed(flight ? client.config().flightSpeed : client.config().elytraSpeed);
        boolean input = client.playing();
        double forward = input ? (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0) : 0;
        double strafe = input ? (mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0) : 0;
        double length = Math.hypot(forward, strafe);
        if (length > 0) {
            forward /= length;
            strafe /= length;
        }

        double yaw = Math.toRadians(player.getYRot());
        double x = (-Math.sin(yaw) * forward - Math.cos(yaw) * strafe) * speed;
        double z = (Math.cos(yaw) * forward - Math.sin(yaw) * strafe) * speed;
        int vertical = input ? (mc.options.keyJump.isDown() ? 1 : 0) - (mc.options.keyShift.isDown() ? 1 : 0) : 0;
        // A slight glide descent keeps normal gliding behaviour when neither vertical key is held.
        double y = vertical == 0 ? (flight ? 0 : -0.01) : vertical * speed * 0.65;

        // This only attempts to avoid the vanilla floating check; servers may still reject flight.
        if (flight && client.config().flightAntiKick && !player.onGround()
            && player.tickCount % 40 == 0 && y > -0.04) {
            y = -0.04;
        }

        int chunkX = Mth.floor(player.getX() + x) >> 4;
        int chunkZ = Mth.floor(player.getZ() + z) >> 4;
        if (!mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) {
            x = 0;
            z = 0;
        }

        Vec3 movement = new Vec3(x, y, z);
        player.setDeltaMovement(movement);
        return movement;
    }

    private static boolean canControlGlider(LocalPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return player.isFallFlying() && chest.has(DataComponents.GLIDER)
            && LivingEntity.canGlideUsing(chest, EquipmentSlot.CHEST);
    }

    private static double boundedSpeed(double speed) {
        return Double.isFinite(speed) ? Math.clamp(speed, 0.05, 4.0) : 0.8;
    }
}
