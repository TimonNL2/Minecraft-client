package nl.timon.liteclient.modules;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;

/** State shared by the main thread and vanilla chunk compilation workers. */
public final class XrayController {
    private static final Set<Block> VISIBLE_BLOCKS = Set.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS,
        Blocks.RAW_IRON_BLOCK, Blocks.RAW_COPPER_BLOCK, Blocks.RAW_GOLD_BLOCK
    );

    // Workers only read this snapshot: they never access the mutable mod config.
    private static volatile boolean active;
    private static boolean savedSmartCull;
    private static boolean ownsSmartCull;

    private XrayController() { }

    public static boolean isActive() {
        return active;
    }

    public static boolean isVisible(BlockState state) {
        return VISIBLE_BLOCKS.contains(state.getBlock());
    }

    /** Call on the client thread after toggling Xray or entering a world. */
    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        boolean enabled = LiteClient.get().isEnabled(ModuleId.XRAY);
        active = enabled;
        if (enabled) {
            if (!ownsSmartCull) {
                savedSmartCull = client.smartCull;
                ownsSmartCull = true;
            }
            client.smartCull = false;
        } else {
            restoreCulling(client);
        }
        // This uses vanilla's invalidation path, including existing worker jobs.
        if (client.level != null) client.levelExtractor.allChanged();
    }

    /** Release renderer state when disconnecting; does not change the saved toggle. */
    public static void reset() {
        boolean wasActive = active;
        active = false;
        Minecraft client = Minecraft.getInstance();
        restoreCulling(client);
        if (wasActive && client.level != null) client.levelExtractor.allChanged();
    }

    private static void restoreCulling(Minecraft client) {
        if (ownsSmartCull) {
            client.smartCull = savedSmartCull;
            ownsSmartCull = false;
        }
    }
}
