package gr1mly4memes.parallix.mixin.entity_parallel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Villager-specific synchronization for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.VillagerMixin
 */
@Mixin(Villager.class)
public class VillagerMixin {

    @Unique
    private static final Object lock = new Object();

    @WrapMethod(method = "pickUpItem")
    private void pickUpItem(ServerLevel level, ItemEntity entity, Operation<Void> original) {
        synchronized (lock) {
            if (!entity.isRemoved()) {
                original.call(level, entity);
            }
        }
    }

    @WrapMethod(method = "spawnGolemIfNeeded")
    private void spawnGolemIfNeeded(ServerLevel level, long timestamp, int villagersNeededToAgree, Operation<Void> original) {
        synchronized (lock) {
            original.call(level, timestamp, villagersNeededToAgree);
        }
    }
}
