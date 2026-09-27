/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;

/** Standalone version of Meteor Criticals' mace smash packets for Minecraft 26.3. */
public final class MaceSpoof extends Module {
    private final Setting<Double> fallHeight = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("fall-height")
        .description("Simulated fall height. Servers may reject large heights or block the effect.")
        .defaultValue(10.0)
        .range(1.501, 100)
        .sliderRange(1.501, 100)
        .build()
    );

    public MaceSpoof() {
        super(Categories.Combat, "mace-spoof", "Simulates a fall when you attack with a mace. Server-dependent.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof ServerboundAttackPacket(int entityId))) return;
        if (mc.player == null || mc.level == null || mc.player.isSpectator()
            || mc.player.isPassenger() || mc.player.isFallFlying()
            || mc.player.isInWater() || mc.player.isInLava() || mc.player.onClimbable()
            || !(mc.player.getMainHandItem().getItem() instanceof MaceItem)) return;
        if (!(mc.level.getEntity(entityId) instanceof LivingEntity target) || !target.isAlive()) return;

        // Never move through a ceiling. Restrict the spoof to a clear vertical path.
        double height = fallHeight.get();
        for (double offset = 0.5; offset < height; offset += 0.5) {
            if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(0, offset, 0))) {
                height = offset - 0.5;
                break;
            }
        }
        if (height < 1.501 || !mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(0, height, 0))) return;

        // 26.3 rejects two position packets in the same client tick. Delimit each
        // simulated movement, including the normal movement preceding/following it.
        mc.player.connection.send(ServerboundClientTickEndPacket.INSTANCE);
        sendPosition(height);
        mc.player.connection.send(ServerboundClientTickEndPacket.INSTANCE);
        sendPosition(0);
        mc.player.connection.send(ServerboundClientTickEndPacket.INSTANCE);
    }

    private void sendPosition(double height) {
        var packet = new ServerboundMovePlayerPacket.Pos(
            mc.player.getX(), mc.player.getY() + height, mc.player.getZ(), false, false);
        // Preserve the airborne flag when No Fall is enabled, just as Criticals does.
        ((IServerboundMovePlayerPacket) packet).meteor$setTag(1337);
        mc.player.connection.send(packet);
    }
}
