package nl.timon.liteclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import nl.timon.liteclient.modules.AutomationController;
import nl.timon.liteclient.modules.PacketController;
import nl.timon.liteclient.modules.XrayController;
import nl.timon.liteclient.ui.ClientScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;

public final class LiteClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("timonclient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiteClient instance;
    private final EnumSet<ModuleId> enabled = EnumSet.noneOf(ModuleId.class);
    private ClientConfig config = new ClientConfig();
    private Path configPath;
    private KeyMapping menuKey;
    private KeyMapping disableKey;
    private LocalPlayer lastPlayer;

    public static LiteClient get() { return instance; }
    public ClientConfig config() { return config; }
    public boolean isEnabled(ModuleId module) { return enabled.contains(module); }

    @Override public void onInitializeClient() {
        instance = this;
        configPath = FabricLoader.getInstance().getConfigDir().resolve("timonclient.json");
        load();
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("timonclient", "controls"));
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.timonclient.menu",
            InputConstants.KEY_RSHIFT, category));
        disableKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.timonclient.disable_all",
            InputConstants.KEY_F8, category));
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            disableAll();
            lastPlayer = null;
        });
        LOGGER.info("Timon Client ready. Right Shift: menu. F8: disable all modules.");
    }

    /** Automated inputs never continue in menus, in the background, or after death. */
    public boolean playing() {
        Minecraft mc = Minecraft.getInstance();
        return inWorld() && mc.gui.screen() == null && mc.isWindowActive();
    }

    public boolean inWorld() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.level != null && mc.gameMode != null
            && mc.player.isAlive() && !mc.player.isSpectator() && !mc.player.isCreative()
            && !mc.isPaused();
    }

    private void tick(Minecraft mc) {
        if (lastPlayer != mc.player) {
            disableAll();
            lastPlayer = mc.player;
        }
        if (mc.player != null && !mc.player.isAlive() && !enabled.isEmpty()) disableAll();
        while (disableKey.consumeClick()) {
            disableAll();
            message("Alle modules uitgeschakeld.");
        }
        while (menuKey.consumeClick()) {
            if (mc.gui.screen() instanceof ClientScreen screen) screen.onClose();
            else if (mc.gui.screen() == null) mc.gui.setScreen(new ClientScreen());
        }
        PacketController.tick();
        AutomationController.tick();
    }

    public void setEnabled(ModuleId module, boolean value) {
        if (value && module == ModuleId.XRAY && (FabricLoader.getInstance().isModLoaded("sodium")
                || FabricLoader.getInstance().isModLoaded("iris"))) {
            message("Xray vereist de standaard Minecraft-renderer. Gebruik een profiel zonder Sodium/Iris.");
            return;
        }
        if (value) {
            if (module == ModuleId.FLIGHT) enabled.remove(ModuleId.ELYTRA_FLY);
            if (module == ModuleId.ELYTRA_FLY) enabled.remove(ModuleId.FLIGHT);
            enabled.add(module);
        } else enabled.remove(module);
        if (module == ModuleId.XRAY) XrayController.refresh();
        if (module == ModuleId.AUTO_EAT && !value) AutomationController.stopEating();
        if (module == ModuleId.ANTI_HUNGER || module == ModuleId.NO_FALL) PacketController.reset();
        message(module.displayName() + (value ? " aan" : " uit"));
    }

    public void disableAll() {
        enabled.clear();
        AutomationController.reset();
        PacketController.reset();
        XrayController.reset();
    }

    public void message(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.gui.hud.setOverlayMessage(Component.literal("Timon • " + text), false);
    }

    private void load() {
        if (!Files.exists(configPath)) return;
        try (var reader = Files.newBufferedReader(configPath)) {
            ClientConfig loaded = GSON.fromJson(reader, ClientConfig.class);
            if (loaded != null) config = loaded;
            config.sanitize();
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Cannot read {}; using defaults", configPath, e);
        }
    }

    public void save() {
        config.sanitize();
        try {
            Files.createDirectories(configPath.getParent());
            Path temporary = configPath.resolveSibling("timonclient.json.tmp");
            Files.writeString(temporary, GSON.toJson(config));
            try {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot save config", e);
            message("Instellingen konden niet worden opgeslagen; zie latest.log.");
        }
    }
}
