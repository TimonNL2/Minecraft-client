package nl.timon.liteclient.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import nl.timon.liteclient.modules.MovementController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyVariable(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 liteclient$adjustMovement(Vec3 movement, MoverType type, Vec3 original) {
        return MovementController.adjust((Entity) (Object) this, type, movement);
    }
}
