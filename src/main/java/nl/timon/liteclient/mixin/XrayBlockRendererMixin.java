package nl.timon.liteclient.mixin;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import nl.timon.liteclient.modules.XrayController;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Filters only world block meshes, leaving inventory item rendering intact. */
@Mixin(ModelBlockRenderer.class)
public abstract class XrayBlockRendererMixin {
    @Shadow @Final private QuadInstance quadInstance;

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void liteclient$filterBlock(BlockQuadOutput output, float x, float y, float z,
            BlockAndTintGetter level, BlockPos pos, BlockState state,
            BlockStateModel model, long seed, CallbackInfo ci) {
        if (XrayController.isActive() && !XrayController.isVisible(state)) ci.cancel();
    }

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private void liteclient$exposeOreFaces(BlockAndTintGetter level, BlockState state,
            Direction direction, BlockPos neighborPos, CallbackInfoReturnable<Boolean> cir) {
        if (XrayController.isActive() && XrayController.isVisible(state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "putQuadWithTint", at = @At("HEAD"))
    private void liteclient$lightOre(BlockQuadOutput output, float x, float y, float z,
            BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad,
            CallbackInfo ci) {
        if (XrayController.isActive() && XrayController.isVisible(state)) {
            // Full sky/block light and no ambient-occlusion blackening underground.
            quadInstance.setLightCoords(0x00F000F0);
            quadInstance.setColor(0xFFFFFFFF);
        }
    }
}
