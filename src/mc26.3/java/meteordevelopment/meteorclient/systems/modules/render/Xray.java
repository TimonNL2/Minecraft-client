/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.MixinPlugin;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.AmbientOcclusionEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.mixin.BlockEntityRenderStateAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;
import java.util.HashSet;

public class Xray extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHighlights = settings.createGroup("Whitelist Highlight");
    private final XrayHighlights highlights = new XrayHighlights();

    public final Setting<Boolean> highlightWhitelist = sgHighlights.add(new BoolSetting.Builder()
        .name("highlight-whitelist")
        .description("Marks whitelisted blocks with a bright box through walls, including spawners.")
        .defaultValue(false)
        .build()
    );

    public final Setting<ShapeMode> highlightShape = sgHighlights.add(new EnumSetting.Builder<ShapeMode>()
        .name("highlight-shape")
        .description("Lines outlines blocks, Sides colors them, Both does both.")
        .defaultValue(ShapeMode.Lines)
        .visible(highlightWhitelist::get)
        .build()
    );

    public final Setting<SettingColor> highlightColor = sgHighlights.add(new ColorSetting.Builder()
        .name("highlight-color")
        .description("The highlight color. Alpha controls opacity, including the colored fill.")
        .defaultValue(new SettingColor(255, 60, 220, 255))
        .visible(highlightWhitelist::get)
        .build()
    );

    public final Setting<Integer> highlightRange = sgHighlights.add(new IntSetting.Builder()
        .name("highlight-range")
        .description("Maximum distance in blocks for highlights in loaded chunks.")
        .defaultValue(64)
        .range(8, 128)
        .sliderRange(8, 128)
        .visible(highlightWhitelist::get)
        .build()
    );

    public final Setting<Integer> highlightLimit = sgHighlights.add(new IntSetting.Builder()
        .name("highlight-limit")
        .description("Maximum number of highlighted blocks. Lower this to improve performance with common blocks.")
        .defaultValue(4096)
        .range(64, 32768)
        .sliderRange(64, 8192)
        .visible(highlightWhitelist::get)
        .build()
    );

    public static final List<Block> ORES = List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS);

    public final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("list-mode")
        .description("Whitelist shows selected blocks. Blacklist makes selected blocks transparent.")
        .defaultValue(ListMode.Whitelist)
        .onChanged(_ -> refreshChunks())
        .build()
    );

    public final Setting<List<Block>> blacklist = sgGeneral.add(new BlockListSetting.Builder()
        .name("blacklist")
        .description("Blocks to make transparent in Blacklist mode.")
        .defaultValue(Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT, Blocks.GRAVEL, Blocks.NETHERRACK)
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .onChanged(_ -> refreshChunks())
        .build()
    );

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Which blocks to show x-rayed.")
        .defaultValue(ORES)
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .onChanged(_ -> {
            if (isActive()) {
                mc.levelExtractor.allChanged();
            }
        })
        .build()
    );

    public final Setting<Integer> opacity = sgGeneral.add(new IntSetting.Builder()
        .name("opacity")
        .description("The opacity for all other blocks.")
        .defaultValue(25)
        .range(0, 255)
        .sliderMax(255)
        .onChanged(_ -> {
            if (isActive()) {
                mc.levelExtractor.allChanged();
            }
        })
        .build()
    );

    private final Setting<FluidOpacity> fluidOpacity = sgGeneral.add(new EnumSetting.Builder<FluidOpacity>()
        .name("fluid-opacity")
        .description("Which fluids should use xray opacity.")
        .defaultValue(FluidOpacity.Both)
        .onChanged(_ -> {
            if (isActive()) {
                mc.levelExtractor.allChanged();
            }
        })
        .build()
    );

    private final Setting<Boolean> exposedOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("exposed-only")
        .description("Show only exposed ores.")
        .defaultValue(false)
        .onChanged(_ -> {
            if (isActive()) {
                mc.levelExtractor.allChanged();
            }
        })
        .build());

    public Xray() {
        super(Categories.Render, "xray", "Only renders specified blocks. Good for mining.");
    }

    @Override
    public void onActivate() {
        highlights.clear();
        mc.levelExtractor.allChanged();
    }

    @Override
    public void onDeactivate() {
        highlights.clear();
        mc.levelExtractor.allChanged();
    }

    @EventHandler
    private void onHighlightTick(TickEvent.Post event) {
        if (!highlightWhitelist.get() || listMode.get() != ListMode.Whitelist || mc.level == null || mc.player == null) {
            highlights.clear();
            return;
        }
        highlights.tick(new HashSet<>(blocks.get()), highlightRange.get(), highlightLimit.get());
    }

    @EventHandler
    private void onHighlightChunk(ChunkDataEvent event) {
        highlights.invalidate(event.chunk());
    }

    @EventHandler
    private void onHighlightBlock(BlockUpdateEvent event) {
        highlights.update(event.pos, event.newState.getBlock());
    }

    @EventHandler
    private void onHighlightLeave(GameLeftEvent event) {
        highlights.clear();
    }

    /** Uses the same eligibility checks as the overlay renderer. */
    public boolean isHighlighted(BlockPos pos) {
        return isActive() && highlightWhitelist.get() && listMode.get() == ListMode.Whitelist
            && mc.level != null && mc.player != null && highlights.positions.contains(pos.asLong())
            && pos.distToCenterSqr(mc.player.getX(), mc.player.getY(), mc.player.getZ()) <= highlightRange.get() * highlightRange.get()
            && !isBlocked(mc.level.getBlockState(pos).getBlock(), pos);
    }

    @EventHandler
    private void onHighlightRender(Render3DEvent event) {
        if (!highlightWhitelist.get() || listMode.get() != ListMode.Whitelist || mc.level == null || mc.player == null) return;
        Color color = highlightColor.get();
        var iterator = highlights.positions.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = BlockPos.of(iterator.nextLong());
            if (isHighlighted(pos)) event.renderer.box(pos, color, color, highlightShape.get(), 0);
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse())
            return theme.label("Warning: Due to shaders in use, opacity is overridden to 0.");

        return null;
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        BlockState state = ((BlockEntityRenderStateAccessor) event.blockEntityState).meteor$getBlockState();
        if (getAlpha(state, event.blockEntityState.blockPos) == 0) event.cancel();
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1;
    }

    public boolean modifyDrawSide(BlockState state, BlockGetter view, BlockPos pos, Direction facing, boolean returns) {
        if (!returns && !isBlocked(state.getBlock(), pos)) {
            BlockPos adjPos = pos.relative(facing);
            BlockState adjState = view.getBlockState(adjPos);
            return adjState.getFaceOcclusionShape(facing.getOpposite()) != Shapes.block() || adjState.getBlock() != state.getBlock() || !adjState.isSolidRender() || isBlocked(adjState.getBlock(), adjPos);
        }

        return returns;
    }

    public boolean isBlocked(Block block, BlockPos blockPos) {
        boolean selected = listMode.get() == ListMode.Whitelist ? blocks.get().contains(block) : !blacklist.get().contains(block);
        return !(selected && (!exposedOnly.get() || blockPos == null || BlockUtils.isExposed(blockPos)));
    }

    private void refreshChunks() {
        if (isActive() && mc.level != null) mc.levelExtractor.allChanged();
    }

    public enum ListMode { Whitelist, Blacklist }

    public static int getAlpha(BlockState state, BlockPos pos) {
        WallHack wallHack = Modules.get().get(WallHack.class);
        Xray xray = Modules.get().get(Xray.class);
        Block block = state.getBlock();

        if (wallHack.isActive() && wallHack.blocks.get().contains(block)) {
            if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) return 0;

            int alpha;

            if (xray.isActive()) alpha = xray.opacity.get();
            else alpha = wallHack.opacity.get();

            return alpha;
        } else if (xray.isActive() && !wallHack.isActive() && xray.isBlocked(block, pos)) {
            return (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) ? 0 : xray.opacity.get();
        }

        return -1;
    }

    public static int getFluidAlpha(FluidState state, BlockPos pos) {
        WallHack wallHack = Modules.get().get(WallHack.class);
        Xray xray = Modules.get().get(Xray.class);
        Block fluidBlock = state.createLegacyBlock().getBlock();

        if (wallHack.isActive() && wallHack.blocks.get().contains(fluidBlock)) {
            if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) return 0;

            return xray.isActive() ? xray.opacity.get() : wallHack.opacity.get();
        } else if (xray.isActive() && !wallHack.isActive() && xray.shouldApplyFluidOpacity(state) && xray.isBlocked(fluidBlock, pos)) {
            return (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) ? 0 : xray.opacity.get();
        }

        return -1;
    }

    private boolean shouldApplyFluidOpacity(FluidState state) {
        return switch (fluidOpacity.get()) {
            case None -> false;
            case Water -> state.is(FluidTags.WATER);
            case Lava -> state.is(FluidTags.LAVA);
            case Both -> state.is(FluidTags.WATER) || state.is(FluidTags.LAVA);
        };
    }

    public enum FluidOpacity {
        None,
        Water,
        Lava,
        Both
    }
}
