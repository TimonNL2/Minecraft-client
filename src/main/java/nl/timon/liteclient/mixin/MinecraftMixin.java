package nl.timon.liteclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;
import nl.timon.liteclient.modules.AutomationController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    // Synthetic eating input does not mutate the user's physical key state.
    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean timon$eatingInput(KeyMapping key, Operation<Boolean> original) {
        if (AutomationController.isEating()) {
            Minecraft mc = (Minecraft) (Object) this;
            if (key == mc.options.keyUse) return true;
            if (key == mc.options.keyAttack) return false;
        }
        return original.call(key);
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void timon$attack(CallbackInfoReturnable<Boolean> ci) {
        if (AutomationController.isEating() || suppressClick(false)) ci.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void timon$use(CallbackInfo ci) {
        if (AutomationController.isEating() || suppressClick(true) || AutomationController.tryAirPlace()) ci.cancel();
    }

    private static boolean suppressClick(boolean right) {
        LiteClient client = LiteClient.get();
        return client != null && client.playing() && client.isEnabled(ModuleId.AUTO_CLICKER)
            && client.config().clickRight == right && !AutomationController.isPerformingClick();
    }
}
