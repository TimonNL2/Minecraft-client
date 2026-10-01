package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Incremental, client-thread-only scanning of loaded chunks. */
final class XrayHighlights {
    final LongOpenHashSet positions = new LongOpenHashSet();
    private final Map<Long, ChunkAccess> chunks = new HashMap<>();
    private final ArrayDeque<Scan> pending = new ArrayDeque<>();
    private ClientLevel level;
    private Set<Block> blocks = Set.of();
    private BlockPos origin;
    private int range, limit, ticks;

    void clear() {
        positions.clear();
        chunks.clear();
        pending.clear();
        level = null;
        origin = null;
        ticks = 0;
    }

    void tick(Set<Block> selected, int radius, int maximum) {
        BlockPos player = mc.player.blockPosition();
        if (level != mc.level || !blocks.equals(selected) || range != radius || limit != maximum
            || origin == null || origin.distSqr(player) >= 64) {
            clear();
            level = mc.level;
            blocks = Set.copyOf(selected);
            origin = player.immutable();
            range = radius;
            limit = maximum;
        }

        if (ticks++ % 20 == 0) refreshChunks();

        // Inspect at most 64 palettes and scan at most four matching sections per tick.
        int palettes = 64;
        for (int budget = 4; budget > 0 && palettes-- > 0 && !pending.isEmpty() && positions.size() < limit; budget--) {
            Scan scan = pending.peek();
            if (chunks.get(scan.chunk.getPos().pack()) != scan.chunk || scan.section >= scan.chunk.getSections().length) {
                pending.remove();
                budget++;
                continue;
            }
            int index = scan.section++;
            int baseY = scan.chunk.getMinY() + index * 16;
            var section = scan.chunk.getSections()[index];
            if (baseY > origin.getY() + range + 8 || baseY + 15 < origin.getY() - range - 8
                || section.hasOnlyAir() || !section.maybeHas(state -> blocks.contains(state.getBlock()))) {
                budget++;
                continue;
            }

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (!blocks.contains(section.getBlockState(x, y, z).getBlock())) continue;
                        pos.set(scan.chunk.getPos().getMinBlockX() + x, baseY + y, scan.chunk.getPos().getMinBlockZ() + z);
                        if (pos.distSqr(origin) <= (range + 8) * (range + 8)) positions.add(pos.asLong());
                        if (positions.size() >= limit) {
                            // Revisit this section if an update frees a highlight slot.
                            scan.section--;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void refreshChunks() {
        var loaded = new ArrayList<ChunkAccess>();
        for (ChunkAccess chunk : Utils.chunks()) {
            double dx = chunk.getPos().getMinBlockX() + 8 - origin.getX();
            double dz = chunk.getPos().getMinBlockZ() + 8 - origin.getZ();
            if (dx * dx + dz * dz <= (range + 24) * (range + 24)) loaded.add(chunk);
        }
        loaded.sort(Comparator.comparingDouble(chunk -> {
            double dx = chunk.getPos().getMinBlockX() + 8 - origin.getX();
            double dz = chunk.getPos().getMinBlockZ() + 8 - origin.getZ();
            return dx * dx + dz * dz;
        }));
        Set<Long> keys = new HashSet<>();
        for (ChunkAccess chunk : loaded) {
            long key = chunk.getPos().pack();
            keys.add(key);
            if (chunks.get(key) != chunk) invalidate(chunk);
        }
        chunks.keySet().retainAll(keys);
        var iterator = positions.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = BlockPos.of(iterator.nextLong());
            if (!keys.contains(net.minecraft.world.level.ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4))) iterator.remove();
        }
    }

    void invalidate(ChunkAccess chunk) {
        if (origin == null) return;
        long key = chunk.getPos().pack();
        if (!chunks.containsKey(key)) {
            double dx = chunk.getPos().getMinBlockX() + 8 - origin.getX();
            double dz = chunk.getPos().getMinBlockZ() + 8 - origin.getZ();
            if (dx * dx + dz * dz > (range + 24) * (range + 24)) return;
        }
        chunks.put(key, chunk);
        pending.removeIf(scan -> scan.chunk.getPos().pack() == key);
        var iterator = positions.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = BlockPos.of(iterator.nextLong());
            if ((pos.getX() >> 4) == chunk.getPos().x() && (pos.getZ() >> 4) == chunk.getPos().z()) iterator.remove();
        }
        pending.add(new Scan(chunk));
    }

    void update(BlockPos pos, Block block) {
        positions.remove(pos.asLong());
        if (origin != null && blocks.contains(block) && positions.size() < limit
            && pos.distSqr(origin) <= (range + 8) * (range + 8)) positions.add(pos.asLong());
    }

    private static final class Scan {
        final ChunkAccess chunk;
        int section;

        Scan(ChunkAccess chunk) { this.chunk = chunk; }
    }
}
