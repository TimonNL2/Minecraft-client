package nl.timon.liteclient;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import nl.timon.liteclient.modules.AutomationController;
import nl.timon.liteclient.modules.XrayController;
import nl.timon.liteclient.ui.ClientScreen;

/** Starts the real client and a disposable world; never uses the user's Minecraft profile. */
public final class ClientSmokeTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            check(LiteClient.get() != null, "Client entrypoint did not run");
            for (ModuleId id : ModuleId.values()) check(!LiteClient.get().isEnabled(id), "Unexpected enabled module");
        });
        context.setScreen(() -> new ClientScreen(new TitleScreen()));
        context.waitTicks(2);
        context.takeScreenshot("timon-menu");
        context.clickScreenButton("Instellingen");
        context.waitTicks(2);
        context.takeScreenshot("timon-settings");
        context.clickScreenButton("Klaar");
        context.waitForScreen(TitleScreen.class);

        try (var world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setGameMode(GameType.SURVIVAL);
                player.getFoodData().setFoodLevel(6);
                player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
                player.getInventory().setItem(1, new ItemStack(Items.COOKED_BEEF, 8));
                player.inventoryMenu.broadcastChanges();
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);
            context.runOnClient(mc -> {
                check(LiteClient.get().inWorld(), "Survival world not ready");
                for (ModuleId id : ModuleId.values()) {
                    LiteClient.get().setEnabled(id, true);
                    check(LiteClient.get().isEnabled(id), "Cannot enable " + id);
                }
                check(!LiteClient.get().isEnabled(ModuleId.FLIGHT), "Movement modes should be exclusive");
                check(XrayController.isActive(), "Xray did not update renderer snapshot");
                LiteClient.get().disableAll();
                check(!XrayController.isActive(), "Xray not restored");
                check(!AutomationController.isEating(), "Eating input leaked");
                LiteClient.get().setEnabled(ModuleId.AUTO_EAT, true);
            });
            context.waitFor(mc -> mc.player.getFoodData().getFoodLevel() > 6, 160);
            context.waitFor(mc -> !AutomationController.isEating(), 120);
            context.runOnClient(mc -> {
                check(mc.player.getInventory().getSelectedSlot() == 0, "Food slot was not restored");
                check(!mc.options.keyUse.isDown(), "Use key stuck after eating");
                LiteClient.get().disableAll();
            });

            // Exercise real movement ticks instead of merely checking module toggles.
            double initialY = context.computeOnClient(mc -> mc.player.getY());
            context.runOnClient(mc -> LiteClient.get().setEnabled(ModuleId.FLIGHT, true));
            context.getInput().holdKeyFor(options -> options.keyJump, 6);
            context.runOnClient(mc -> check(mc.player.getY() > initialY + 1.0, "Flight did not move upward"));
            double hoverY = context.computeOnClient(mc -> mc.player.getY());
            context.waitTicks(6);
            context.runOnClient(mc -> {
                check(Math.abs(mc.player.getY() - hoverY) < 0.15, "Flight failed to hover");
                LiteClient.get().disableAll();
            });
            context.waitTicks(25);

            // Validate a real air placement acknowledged by the server.
            context.getInput().lookAt(180, -15);
            context.waitTicks(2);
            BlockPos airTarget = context.computeOnClient(mc -> BlockPos.containing(mc.player.getEyePosition()
                .add(mc.player.getLookAngle().scale(LiteClient.get().config().airPlaceReach))));
            context.runOnClient(mc -> LiteClient.get().setEnabled(ModuleId.AIR_PLACE, true));
            context.getInput().pressKey(options -> options.keyUse);
            world.getServer().waitFor(server -> world.getConnection().getServerLevel().getBlockState(airTarget).is(Blocks.STONE), 40);
            context.runOnClient(mc -> LiteClient.get().disableAll());

            // Keep Xray active across a real mesh rebuild to exercise render injections.
            world.getServer().runCommand("fill -2 -60 3 2 -57 7 minecraft:stone");
            world.getServer().runCommand("setblock 0 -59 5 minecraft:diamond_ore");
            context.getInput().lookAt(0, 0);
            world.getConnection().waitForClientboundPackets();
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("timon-normal-world");
            context.runOnClient(mc -> LiteClient.get().setEnabled(ModuleId.XRAY, true));
            world.getConnection().waitForChunksRender();
            context.waitTicks(5);
            context.takeScreenshot("timon-xray-world");
            context.runOnClient(mc -> LiteClient.get().disableAll());

            // A real fall checks the server result, not just the outgoing packet flag.
            context.runOnClient(mc -> LiteClient.get().setEnabled(ModuleId.NO_FALL, true));
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setHealth(20);
                player.teleportTo(0.5, -42, 0.5);
            });
            context.waitFor(mc -> mc.player.getY() > -43, 40);
            context.waitFor(mc -> mc.player.onGround() && mc.player.getY() < -59, 160);
            world.getConnection().waitForServerboundPackets();
            world.getServer().runOnServer(server -> check(world.getConnection().getServerPlayer().getHealth() == 20,
                "No Fall did not prevent fall damage on the vanilla test server"));
            context.runOnClient(mc -> LiteClient.get().disableAll());

            // Elytra flight requires a real equipped glider and a normal start-gliding action.
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
                player.teleportTo(0.5, -30, 0.5);
            });
            context.waitFor(mc -> mc.player.getY() > -32 && mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA), 60);
            context.waitFor(mc -> !mc.player.onGround() && mc.player.getDeltaMovement().y < 0, 40);
            context.runOnClient(mc -> LiteClient.get().setEnabled(ModuleId.ELYTRA_FLY, true));
            context.getInput().holdKeyFor(options -> options.keyJump, 2);
            context.waitFor(mc -> mc.player.isFallFlying(), 40);
            double glideZ = context.computeOnClient(mc -> mc.player.getZ());
            context.getInput().holdKeyFor(options -> options.keyUp, 6);
            context.runOnClient(mc -> {
                check(mc.player.getZ() > glideZ + 3, "Elytra Fly did not apply configured movement");
                LiteClient.get().disableAll();
            });
        }
        context.waitForScreen(TitleScreen.class);
        context.runOnClient(mc -> {
            for (ModuleId id : ModuleId.values()) check(!LiteClient.get().isEnabled(id), "Module survived disconnect");
        });
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
