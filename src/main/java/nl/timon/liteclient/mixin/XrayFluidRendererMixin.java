package nl.timon.liteclient.mixin;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import nl.timon.liteclient.modules.XrayController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class XrayFluidRendererMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void liteclient$hideFluids(BlockAndTintGetter level, BlockPos pos,
            FluidRenderer.Output output, BlockState state, FluidState fluid,
            CallbackInfo ci) {
        if (XrayController.isActive()) ci.cancel();
    }
}
