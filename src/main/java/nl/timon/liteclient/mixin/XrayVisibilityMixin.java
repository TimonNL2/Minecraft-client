package nl.timon.liteclient.mixin;

import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import nl.timon.liteclient.modules.XrayController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VisGraph.class)
public abstract class XrayVisibilityMixin {
    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
    private void liteclient$openSectionVisibility(CallbackInfoReturnable<VisibilitySet> cir) {
        if (XrayController.isActive()) {
            VisibilitySet visibility = new VisibilitySet();
            visibility.setAll(true);
            cir.setReturnValue(visibility);
        }
    }
}
