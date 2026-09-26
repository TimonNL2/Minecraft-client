package nl.timon.liteclient.mixin;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import nl.timon.liteclient.modules.XrayController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric's bundled Indigo renderer replaces the vanilla block mesh entry point. */
@Mixin(value = AltModelBlockRendererImpl.class, remap = false)
public abstract class XrayIndigoMixin {
    @Shadow private BlockState blockState;

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void timon$filter(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level,
            BlockPos pos, BlockState state, BlockStateModel model, long seed, CallbackInfo ci) {
        if (XrayController.isActive() && !XrayController.isVisible(state)) ci.cancel();
    }

    @Inject(method = "shouldCullFace", at = @At("HEAD"), cancellable = true)
    private void timon$faces(Direction direction, CallbackInfoReturnable<Boolean> ci) {
        if (XrayController.isActive() && blockState != null && XrayController.isVisible(blockState))
            ci.setReturnValue(false);
    }

    @Inject(method = "transform", at = @At("HEAD"))
    private void timon$light(MutableQuadView quad, CallbackInfoReturnable<Boolean> ci) {
        if (XrayController.isActive() && blockState != null && XrayController.isVisible(blockState)) {
            quad.emissive(true);
            quad.ambientOcclusion(TriState.FALSE);
        }
    }
}
