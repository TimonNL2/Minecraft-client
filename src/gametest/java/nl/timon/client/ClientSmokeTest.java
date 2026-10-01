package nl.timon.client;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.render.WallHack;
import meteordevelopment.meteorclient.timon.ClientFeatures;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/** Real client, real renderer and disposable integrated survival server. */
public final class ClientSmokeTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            boolean expectSodium = "sodium".equals(System.getProperty("timon.test.renderer", "vanilla"));
            check(FabricLoader.getInstance().isModLoaded("sodium") == expectSodium, "Test renderer was not loaded as requested");
            MeteorClient.LOG.info("Testing renderer: {}", expectSodium ? "Sodium" : "vanilla/Indigo");
            check(MeteorClient.INSTANCE != null, "Entrypoint did not run");
            check(MeteorClient.FOLDER.getName().equals("timon-client"), "Config would overwrite Meteor's folder");
            Modules.get().disableAll();
            var names = Modules.get().getAll().stream().map(module -> module.name).collect(Collectors.toSet());
            check(names.equals(ClientFeatures.MODULES), "Unexpected feature set: " + names);
            boolean is263 = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
                .getMetadata().getVersion().getFriendlyString().equals("26.3");
            check(Modules.get().getCount() == (is263 ? 12 : 9), "Incorrect module count");
            check((Modules.get().get("mace-spoof") != null) == is263, "Mace Spoof version gating incorrect");
            check((Modules.get().get("storage-esp") != null) == is263, "Storage ESP version gating incorrect");
            check((Modules.get().get("auto-fish") != null) == is263, "Auto Fish version gating incorrect");
            check(Modules.get().searchTitles("wall").stream().allMatch(pair -> ClientFeatures.allows(pair.getFirst().name)), "Hidden module in search");
            Modules.get().get(WallHack.class).enable();
            check(!Modules.get().get(WallHack.class).isActive(), "Internal helper module became enabled");
            check(Modules.get().get("wall-hack") == null, "Hidden module accessible by command");
            for (var module : Modules.get().getAll()) module.settings.reset();
            check(Modules.get().get(ElytraFly.class).settings.get("auto-pilot") != null, "Elytra options missing");
        });
        context.setScreen(TitleScreen::new);
        context.waitTicks(10);
        context.takeScreenshot("vesper-title-screen");
        context.runOnClient(mc -> Tabs.get().getFirst().openScreen(GuiThemes.get()));
        context.waitTicks(5);
        context.takeScreenshot("timon-meteor-menu");
        context.setScreen(() -> new ModuleScreen(GuiThemes.get(), Modules.get().get(Xray.class)));
        context.waitTicks(5);
        context.takeScreenshot("timon-xray-settings");
        context.setScreen(SearchTestScreen::new);
        context.waitTicks(5);
        context.takeScreenshot("timon-block-selector");
        context.runOnClient(mc -> check(org.lwjgl.sdl.SDLKeyboard.SDL_TextInputActive(mc.getWindow().handle()),
            "Block search did not activate SDL text input"));
        context.getInput().typeChars("diamond");
        context.waitTicks(3);
        context.runOnClient(mc -> {
            var screen = (SearchTestScreen) mc.gui.screen();
            check(screen.search().get().equals("diamond"), "Block search did not receive typed text");
            check(screen.itemCount() > 0 && screen.itemCount() < 20, "Block search did not filter diamond results");
        });
        context.takeScreenshot("timon-xray-search-diamond");
        context.getInput().typeChars("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
        context.waitTicks(2);
        context.runOnClient(mc -> check(((SearchTestScreen) mc.gui.screen()).itemCount() == 0, "Unmatched search showed blocks"));
        int searchLength = context.computeOnClient(mc -> ((SearchTestScreen) mc.gui.screen()).search().get().length());
        for (int i = 0; i < searchLength; i++) {
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE);
        }
        context.waitTicks(2);
        context.runOnClient(mc -> {
            var screen = (SearchTestScreen) mc.gui.screen();
            check(screen.search().get().isEmpty() && screen.itemCount() > 100, "Clearing search did not restore blocks: text=" + screen.search().get() + ", items=" + screen.itemCount());
            screen.search().setFocused(false);
            check(!org.lwjgl.sdl.SDLKeyboard.SDL_TextInputActive(mc.getWindow().handle()), "Unfocused search kept SDL text input active");
            screen.search().setFocused(true);
            check(org.lwjgl.sdl.SDLKeyboard.SDL_TextInputActive(mc.getWindow().handle()), "Refocused search did not reactivate text input");
        });
        context.setScreen(TitleScreen::new);

        try (var world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                server.getWorldData().setAllowCommands(false);
                var identity = new NameAndId(player.getGameProfile());
                server.getPlayerList().deop(identity);
                check(!server.getPlayerList().isOp(identity), "Survival fixture must not have OP permissions");
                player.setGameMode(GameType.SURVIVAL);
                player.getFoodData().setFoodLevel(6);
                player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
                player.getInventory().setItem(1, new ItemStack(Items.COOKED_BEEF, 8));
                player.inventoryMenu.broadcastChanges();
            });
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(5);
            context.runOnClient(mc -> {
                // All available modules must activate/deactivate without broken dependencies.
                for (var module : Modules.get().getAll()) {
                    module.enable();
                    check(module.isActive(), "Cannot enable " + module.name);
                    module.disable();
                }
                enable("auto-eat");
            });
            context.waitFor(mc -> mc.player.getFoodData().getFoodLevel() > 6, 160);
            context.waitFor(mc -> !Modules.get().get(AutoEat.class).eating, 160);
            context.runOnClient(mc -> {
                check(mc.player.getInventory().getSelectedSlot() == 0, "Food slot not restored");
                Modules.get().disableAll();
                check(!mc.options.keyUse.isDown(), "Use key stuck after eating");
            });

            double initialY = context.computeOnClient(mc -> mc.player.getY());
            context.runOnClient(mc -> enable("flight"));
            context.getInput().holdKeyFor(options -> options.keyJump, 6);
            context.runOnClient(mc -> check(mc.player.getY() > initialY + 1, "Flight did not lift survival player"));
            context.runOnClient(mc -> {
                // EasyPlaceFix and Tweakeroo send additional rotation-only packets.
                // Flight must not promote these to extra position updates in 26.3.
                var connection = mc.getConnection().getConnection();
                var look = new ServerboundMovePlayerPacket.Rot(90, 20, mc.player.onGround(), false);
                var lookEvent = MeteorClient.EVENT_BUS.post(new PacketEvent.Send(look, connection));
                check(!lookEvent.isCancelled() && lookEvent.packet == look && !look.hasPosition(),
                    "Flight promoted a placement rotation to an extra position packet");
                check(look.getYRot(0) == 90 && look.getXRot(0) == 20, "Flight changed placement rotation");
                var status = new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround(), false);
                var statusEvent = MeteorClient.EVENT_BUS.post(new PacketEvent.Send(status, connection));
                check(!statusEvent.isCancelled() && statusEvent.packet == status && !status.hasPosition(),
                    "Flight promoted a status-only packet to an extra position packet");

                // Exercise the actual server rule with an isolated client-tick packet sequence:
                // two placement rotations plus one ordinary position update must remain connected.
                mc.getConnection().send(ServerboundClientTickEndPacket.INSTANCE);
                mc.getConnection().send(look);
                mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(-90, -20, mc.player.onGround(), false));
                mc.getConnection().send(status);
                mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(),
                    mc.player.onGround(), mc.player.horizontalCollision));
                mc.getConnection().send(ServerboundClientTickEndPacket.INSTANCE);
            });
            world.getConnection().waitForServerboundPackets();
            context.waitTicks(3);
            context.runOnClient(mc -> {
                check(mc.getConnection() != null && mc.getConnection().getConnection().isConnected(),
                    "Flight placement rotations disconnected the client");
                check(Modules.get().get(Flight.class).isActive(), "Placement test unexpectedly disabled Flight");
                MeteorClient.LOG.info("Flight placement rotation compatibility passed");
            });
            context.runOnClient(mc -> {
                Modules.get().disableAll();
                check(!mc.player.getAbilities().mayfly, "Flight did not restore survival abilities");
            });
            context.waitTicks(30);

            context.getInput().lookAt(180, -15);
            context.runOnClient(mc -> {
                var module = Modules.get().get("air-place");
                module.settings.get("custom-range", Boolean.class).set(true);
                module.settings.get("range", Double.class).set(3.0);
                module.enable();
            });
            context.waitTicks(4);
            BlockPos airTarget = context.computeOnClient(mc -> BlockPos.containing(mc.player.getEyePosition().add(mc.player.getLookAngle().scale(3))));
            context.getInput().holdKeyFor(options -> options.keyUse, 2);
            world.getServer().waitFor(server -> world.getConnection().getServerLevel().getBlockState(airTarget).is(Blocks.STONE), 40);
            context.runOnClient(mc -> Modules.get().disableAll());

            world.getServer().runCommand("fill -2 -60 3 2 -57 7 minecraft:stone");
            world.getServer().runCommand("setblock 0 -59 5 minecraft:diamond_ore");
            // Enclosed water and lava exercise Sodium's fluid occlusion hooks,
            // including the path that previously crashed as soon as a world loaded.
            world.getServer().runCommand("fill 3 -60 3 5 -57 7 minecraft:stone");
            world.getServer().runCommand("setblock 4 -59 5 minecraft:water");
            world.getServer().runCommand("fill -5 -60 3 -3 -57 7 minecraft:stone");
            world.getServer().runCommand("setblock -4 -59 5 minecraft:lava");
            context.getInput().lookAt(0, 0);
            world.getConnection().waitForClientboundPackets();
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("timon-normal-world");
            context.runOnClient(mc -> {
                Xray xray = Modules.get().get(Xray.class);
                xray.enable();
                xray.opacity.set(0);
                check(Xray.getAlpha(Blocks.STONE.defaultBlockState(), null) == 0, "Whitelist did not hide stone");
                check(Xray.getAlpha(Blocks.DIAMOND_ORE.defaultBlockState(), null) == -1, "Whitelist hid ore");
            });
            world.getConnection().waitForChunksRender();
            context.waitTicks(5);
            context.takeScreenshot("timon-xray-zero-opacity");
            context.runOnClient(mc -> {
                Xray xray = Modules.get().get(Xray.class);
                xray.opacity.set(80);
                check(Xray.getAlpha(Blocks.STONE.defaultBlockState(), null) == 80, "Opacity ignored");
            });
            world.getConnection().waitForChunksRender();
            context.waitTicks(5);
            context.takeScreenshot("timon-xray-partial-opacity");
            for (Xray.FluidOpacity fluidMode : Xray.FluidOpacity.values()) {
                context.runOnClient(mc -> {
                    Xray xray = Modules.get().get(Xray.class);
                    xray.settings.get("fluid-opacity", Xray.FluidOpacity.class).set(fluidMode);
                    int waterAlpha = fluidMode == Xray.FluidOpacity.Water || fluidMode == Xray.FluidOpacity.Both ? 80 : -1;
                    int lavaAlpha = fluidMode == Xray.FluidOpacity.Lava || fluidMode == Xray.FluidOpacity.Both ? 80 : -1;
                    check(Xray.getFluidAlpha(Blocks.WATER.defaultBlockState().getFluidState(), null) == waterAlpha, "Water opacity mismatch: " + fluidMode);
                    check(Xray.getFluidAlpha(Blocks.LAVA.defaultBlockState().getFluidState(), null) == lavaAlpha, "Lava opacity mismatch: " + fluidMode);
                });
                world.getConnection().waitForChunksRender();
                context.waitTicks(5);
                context.takeScreenshot("timon-xray-fluid-" + fluidMode.name().toLowerCase(java.util.Locale.ROOT));
            }
            context.runOnClient(mc -> {
                Xray xray = Modules.get().get(Xray.class);
                xray.listMode.set(Xray.ListMode.Blacklist);
                xray.blacklist.set(List.of(Blocks.DIAMOND_ORE));
                check(Xray.getAlpha(Blocks.DIAMOND_ORE.defaultBlockState(), null) == 80, "Blacklist did not fade selected ore");
                check(Xray.getAlpha(Blocks.STONE.defaultBlockState(), null) == -1, "Blacklist hid unselected stone");
                var saved = xray.toTag();
                xray.settings.reset();
                xray.fromTag(saved);
                check(xray.listMode.get() == Xray.ListMode.Blacklist && xray.blacklist.get().equals(List.of(Blocks.DIAMOND_ORE)) && xray.opacity.get() == 80, "Xray save/load lost settings");
                Modules.get().disableAll();
                xray.settings.reset();
                enable("no-fall");
            });
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setHealth(20);
                player.teleportTo(0.5, -42, 0.5);
            });
            context.waitFor(mc -> mc.player.getY() > -43, 40);
            context.waitFor(mc -> mc.player.onGround() && mc.player.getY() < -59, 160);
            world.getConnection().waitForServerboundPackets();
            world.getServer().runOnServer(server -> check(world.getConnection().getServerPlayer().getHealth() == 20, "No Fall failed on vanilla survival server"));
            context.runOnClient(mc -> Modules.get().disableAll());

            if (context.computeOnClient(mc -> Modules.get().get("mace-spoof") != null)) {
                world.getServer().runOnServer(server -> {
                    var player = world.getConnection().getServerPlayer();
                    player.getInventory().setItem(0, new ItemStack(Items.MACE));
                    player.getInventory().setSelectedSlot(0);
                    player.inventoryMenu.broadcastChanges();
                });
                world.getServer().runCommand("summon minecraft:cow 2 -60 0.5 {NoAI:1b,PersistenceRequired:1b,Tags:[\"mace-test\"]}");
                world.getConnection().waitForClientboundPackets();
                context.waitTicks(25);
                var targetId = context.computeOnClient(mc -> {
                    for (var entity : mc.level.entitiesForRendering()) {
                        if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("cow")) return entity.getUUID();
                    }
                    throw new AssertionError("Mace test cow not loaded");
                });
                context.runOnClient(mc -> {
                    check(mc.player.onGround(), "Mace test requires a standing player");
                    check(mc.player.getMainHandItem().is(Items.MACE), "Mace was not equipped");
                    mc.gameMode.attack(mc.player, mc.level.getEntity(targetId));
                });
                world.getConnection().waitForServerboundPackets();
                world.getServer().runOnServer(server -> {
                    var target = (net.minecraft.world.entity.LivingEntity) server.overworld().getEntity(targetId);
                    check(target != null && target.isAlive() && target.getHealth() < 10, "Baseline mace attack must damage but not kill cow");
                    target.setHealth(10);
                });
                context.waitTicks(25);
                context.runOnClient(mc -> {
                    var position = mc.player.position();
                    enable("mace-spoof");
                    enable("no-fall");
                    mc.gameMode.attack(mc.player, mc.level.getEntity(targetId));
                    check(mc.player.position().equals(position), "Mace Spoof moved local player");
                });
                world.getConnection().waitForServerboundPackets();
                world.getServer().runOnServer(server -> {
                    var target = server.overworld().getEntity(targetId);
                    check(target == null || !target.isAlive(), "Mace Spoof did not one-hit the restored cow");
                    check(Math.abs(world.getConnection().getServerPlayer().getY() + 60) < 0.1, "Mace Spoof did not restore server position");
                });
                context.runOnClient(mc -> Modules.get().disableAll());
            }

            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
                player.teleportTo(0.5, -30, 0.5);
            });
            context.waitFor(mc -> mc.player.getY() > -32 && mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA), 60);
            context.waitFor(mc -> !mc.player.onGround() && mc.player.getDeltaMovement().y < 0, 40);
            context.runOnClient(mc -> enable("elytra-fly"));
            context.getInput().holdKeyFor(options -> options.keyJump, 2);
            context.waitFor(mc -> mc.player.isFallFlying(), 40);
            double glideZ = context.computeOnClient(mc -> mc.player.getZ());
            context.getInput().holdKeyFor(options -> options.keyUp, 8);
            context.runOnClient(mc -> {
                check(mc.player.getZ() > glideZ + 2, "Elytra Fly did not control movement");
                Modules.get().disableAll();
                enable("fast-use");
                enable("xray");
            });
            context.getInput().pressKey(options -> KeyBinds.DISABLE_ALL);
            context.waitTicks(2);
            context.runOnClient(mc -> {
                check(Modules.get().getActive().isEmpty(), "F8 did not disable all modules");
                MeteorClient.LOG.info("All Vesper module assertions passed without OP; closing disposable world.");
            });
        }
        context.waitForScreen(TitleScreen.class);
        context.runOnClient(mc -> check(Modules.get().getActive().isEmpty(), "Modules remained enabled"));
    }

    private static void enable(String name) { Modules.get().get(name).enable(); }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static final class SearchTestScreen extends BlockListSettingScreen {
        SearchTestScreen() {
            super(GuiThemes.get(), (BlockListSetting) Modules.get().get(Xray.class).blocks);
        }

        private Stream<WWidget> widgets(
            WWidget widget) {
            var children = widget instanceof WContainer container
                ? container.cells.stream().flatMap(cell -> widgets(cell.widget()))
                : Stream.<WWidget>empty();
            return Stream.concat(Stream.of(widget), children);
        }

        WTextBox search() {
            return widgets(window).filter(widget -> widget instanceof WTextBox)
                .map(widget -> (WTextBox) widget).findFirst().orElseThrow();
        }

        long itemCount() {
            return widgets(window).filter(widget -> widget instanceof WItemWithLabel).count();
        }
    }
}
