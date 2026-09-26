package nl.timon.liteclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.MaceItem;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;
import nl.timon.liteclient.mixin.MovePacketAccessor;

/** One policy owns movement flags, so No Fall and Anti Hunger cannot undo each other. */
public final class PacketController {
    private static LocalPlayer trackedPlayer;
    private static boolean lastGrounded;
    private static boolean landingPending;
    private static boolean antiHungerActive;

    private PacketController() {}

    /** Called before the local player's tick. Also reconciles sprint state on setting changes. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LiteClient client = LiteClient.get();
        if (mc.player == null || mc.level == null || client == null) {
            reset();
            return;
        }
        LocalPlayer player = mc.player;
        if (trackedPlayer != player) {
            reset();
            trackedPlayer = player;
            lastGrounded = player.onGround();
        }
        observeGround(player);

        boolean active = client.inWorld() && client.isEnabled(ModuleId.ANTI_HUNGER)
            && ordinaryMovement(player);
        if (active != antiHungerActive) {
            antiHungerActive = active;
            if (player.isSprinting()) {
                // Starting mid-sprint must clear the server's existing sprint state too.
                var action = active ? ServerboundPlayerCommandPacket.Action.STOP_SPRINTING
                    : ServerboundPlayerCommandPacket.Action.START_SPRINTING;
                player.connection.send(new ServerboundPlayerCommandPacket(player, action));
            }
        }
    }

    /** Returns true only when the outgoing packet should be cancelled. */
    public static boolean handle(Packet<?> packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket)
            && !(packet instanceof ServerboundPlayerCommandPacket)) return false;

        Minecraft mc = Minecraft.getInstance();
        // Keep-alives and other off-thread traffic must never access mutable game state here.
        if (!mc.isSameThread()) return false;
        LiteClient client = LiteClient.get();
        LocalPlayer player = mc.player;
        if (client == null || !client.inWorld() || player == null || mc.level == null
            || !ordinaryMovement(player)) return false;

        if (packet instanceof ServerboundPlayerCommandPacket command) {
            return antiHungerActive && client.isEnabled(ModuleId.ANTI_HUNGER)
                && command.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING;
        }

        ServerboundMovePlayerPacket movement = (ServerboundMovePlayerPacket) packet;
        observeGround(player);
        boolean landing = landingPending;
        landingPending = false;

        // Leave gliding and mace attacks alone; their fall state has gameplay meaning.
        if (client.isEnabled(ModuleId.NO_FALL)
            && !(player.getMainHandItem().getItem() instanceof MaceItem)
            && !player.onGround() && player.getDeltaMovement().y < -0.5
            && player.fallDistance > 2.0) {
            ((MovePacketAccessor) movement).liteclient$setOnGround(true);
            return false;
        }

        if (antiHungerActive && client.isEnabled(ModuleId.ANTI_HUNGER) && !landing
            && player.onGround() && player.fallDistance <= 0
            && mc.gameMode != null && !mc.gameMode.isDestroying()) {
            ((MovePacketAccessor) movement).liteclient$setOnGround(false);
        }
        return false;
    }

    public static void reset() {
        LocalPlayer previousPlayer = trackedPlayer;
        boolean restoreSprint = antiHungerActive;
        trackedPlayer = null;
        lastGrounded = false;
        landingPending = false;
        antiHungerActive = false;
        Minecraft mc = Minecraft.getInstance();
        if (restoreSprint && previousPlayer != null && previousPlayer == mc.player
                && mc.getConnection() != null && previousPlayer.isSprinting()) {
            previousPlayer.connection.send(new ServerboundPlayerCommandPacket(previousPlayer,
                ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        }
    }

    private static boolean ordinaryMovement(LocalPlayer player) {
        return !player.isPassenger() && !player.isInLiquid() && !player.isFallFlying()
            && !player.isSleeping();
    }

    private static void observeGround(LocalPlayer player) {
        boolean grounded = player.onGround();
        if (grounded && !lastGrounded) landingPending = true;
        lastGrounded = grounded;
    }
}
