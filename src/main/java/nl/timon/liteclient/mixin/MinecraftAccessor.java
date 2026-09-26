package nl.timon.liteclient.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("rightClickDelay") int timon$getUseDelay();
    @Accessor("rightClickDelay") void timon$setUseDelay(int ticks);
    @Invoker("startAttack") boolean timon$attack();
    @Invoker("startUseItem") void timon$use();
}
