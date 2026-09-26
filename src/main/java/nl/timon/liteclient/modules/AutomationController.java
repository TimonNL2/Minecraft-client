package nl.timon.liteclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import nl.timon.liteclient.LiteClient;
import nl.timon.liteclient.ModuleId;
import nl.timon.liteclient.mixin.MinecraftAccessor;

public final class AutomationController {
    private static final ClickClock CLICK_CLOCK = new ClickClock();
    private static LocalPlayer eatingPlayer;
    private static InteractionHand eatingHand;
    private static int previousSlot = -1;
    private static int foodSlot = -1;
    private static int eatCooldown;
    private static int eatTicks;
    private static int airCooldown;
    private static boolean performingClick;

    private AutomationController() {}
    public static boolean isEating() { return eatingPlayer != null; }
    public static boolean isPerformingClick() { return performingClick; }

    public static void tick() {
        LiteClient client = LiteClient.get();
        Minecraft mc = Minecraft.getInstance();
        if (client == null || !client.playing()) {
            stopEating();
            CLICK_CLOCK.reset();
            return;
        }
        if (eatCooldown > 0) eatCooldown--;
        if (airCooldown > 0) airCooldown--;
        tickEating(client, mc);
        if (isEating() || mc.player.isUsingItem()) {
            CLICK_CLOCK.reset();
            return;
        }
        MinecraftAccessor access = (MinecraftAccessor) mc;
        if (client.isEnabled(ModuleId.FAST_USE)) {
            access.timon$setUseDelay(Math.min(access.timon$getUseDelay(), client.config().fastUseDelay));
        }
        if (!client.isEnabled(ModuleId.AUTO_CLICKER)) {
            CLICK_CLOCK.reset();
            return;
        }
        boolean held = (client.config().clickRight ? mc.options.keyUse : mc.options.keyAttack).isDown();
        if (client.config().clickHold && !held) {
            CLICK_CLOCK.reset();
            return;
        }
        if (CLICK_CLOCK.tick(client.config().clicksPerSecond)) {
            performingClick = true;
            try {
                if (client.config().clickRight) access.timon$use();
                else access.timon$attack();
            } finally {
                performingClick = false;
            }
        }
    }

    private static void tickEating(LiteClient client, Minecraft mc) {
        if (isEating()) {
            // A user's slot change wins over the automatic food selection.
            if (!client.isEnabled(ModuleId.AUTO_EAT) || eatingPlayer != mc.player
                    || (foodSlot >= 0 && mc.player.getInventory().getSelectedSlot() != foodSlot)
                    || !mc.player.isUsingItem() || mc.player.getUsedItemHand() != eatingHand || ++eatTicks > 100) {
                stopEating();
                eatCooldown = 5;
            }
            return;
        }
        if (!client.isEnabled(ModuleId.AUTO_EAT) || eatCooldown > 0 || mc.player.isUsingItem()
                || mc.player.getFoodData().getFoodLevel() > client.config().eatAt) return;

        int bestSlot = -1;
        float bestScore = foodScore(mc.player.getOffhandItem());
        InteractionHand hand = InteractionHand.OFF_HAND;
        for (int slot = 0; slot < 9; slot++) {
            float score = foodScore(mc.player.getInventory().getItem(slot));
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
                hand = InteractionHand.MAIN_HAND;
            }
        }
        if (bestScore <= 0) return;
        eatingPlayer = mc.player;
        eatingHand = hand;
        eatTicks = 0;
        previousSlot = mc.player.getInventory().getSelectedSlot();
        foodSlot = bestSlot;
        if (bestSlot >= 0) selectSlot(mc.player, bestSlot);
        mc.gameMode.useItem(mc.player, hand);
        if (!mc.player.isUsingItem()) {
            stopEating();
            eatCooldown = 10;
        }
    }

    private static float foodScore(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.CONSUMABLE)) return -1;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.SPIDER_EYE) || stack.is(Items.PUFFERFISH)
                || stack.is(Items.POISONOUS_POTATO) || stack.is(Items.CHICKEN) || stack.is(Items.SUSPICIOUS_STEW)
                || stack.is(Items.CHORUS_FRUIT) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return -1;
        var food = stack.get(DataComponents.FOOD);
        return food == null || food.nutrition() <= 0 ? -1 : food.nutrition() + food.saturation();
    }

    public static void stopEating() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer owner = eatingPlayer;
        if (owner == null) return;
        eatingPlayer = null;
        if (owner == mc.player) {
            if (mc.gameMode != null && owner.isUsingItem() && owner.getUsedItemHand() == eatingHand)
                mc.gameMode.releaseUsingItem(owner);
            if (foodSlot >= 0 && previousSlot >= 0 && owner.getInventory().getSelectedSlot() == foodSlot)
                selectSlot(owner, previousSlot);
        }
        previousSlot = foodSlot = -1;
        eatingHand = null;
        eatTicks = 0;
    }

    private static void selectSlot(LocalPlayer player, int slot) {
        if (player.getInventory().getSelectedSlot() == slot) return;
        player.getInventory().setSelectedSlot(slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    /** Uses normal predicted placement and acknowledgement; never invents inventory or blocks. */
    public static boolean tryAirPlace() {
        LiteClient client = LiteClient.get();
        Minecraft mc = Minecraft.getInstance();
        if (client == null || !client.playing() || !client.isEnabled(ModuleId.AIR_PLACE) || isEating()
                || mc.player.isUsingItem() || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.MISS) return false;
        InteractionHand hand;
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem) hand = InteractionHand.MAIN_HAND;
        else if (mc.player.getOffhandItem().getItem() instanceof BlockItem) hand = InteractionHand.OFF_HAND;
        else return false;
        if (airCooldown > 0) return true;
        double range = Math.min(client.config().airPlaceReach, mc.player.blockInteractionRange() - 0.5);
        Vec3 eye = mc.player.getEyePosition();
        BlockPos target = BlockPos.containing(eye.add(mc.player.getLookAngle().scale(range)));
        Vec3 hit = Vec3.atCenterOf(target);
        if (!mc.level.hasChunkAt(target) || !mc.level.isInWorldBounds(target)
                || !mc.level.getWorldBorder().isWithinBounds(target) || !mc.level.getBlockState(target).canBeReplaced()
                || mc.player.getBoundingBox().intersects(new AABB(target))
                || eye.distanceTo(hit) > mc.player.blockInteractionRange()) return false;
        mc.gameMode.useItemOn(mc.player, hand, new BlockHitResult(hit, Direction.UP, target, false));
        airCooldown = client.config().airPlaceDelay;
        ((MinecraftAccessor) mc).timon$setUseDelay(airCooldown);
        return true;
    }

    public static void reset() {
        stopEating();
        CLICK_CLOCK.reset();
        eatCooldown = airCooldown = 0;
        performingClick = false;
    }
}
