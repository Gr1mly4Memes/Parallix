package gr1mly4memes.parallix.mixin.entity_parallel.sensor;

import gr1mly4memes.parallix.common.entity_parallel.SensorUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestItemSensor;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.*;

/**
 * NearestItemSensor optimization with distance caching for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.sensor.NearestItemSensorMixin
 */
@Mixin(value = NearestItemSensor.class, priority = 1500)
public class NearestItemSensorMixin {

    /**
     * @author Async (_Axa_lotL_)
     * @reason async distance cache
     */
    @Overwrite
    protected void doTick(final ServerLevel level, final Mob body) {
        Brain<?> brain = body.getBrain();
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, body.getBoundingBox().inflate(32.0, 16.0, 32.0), _ -> true);
        items.sort(SensorUtils.distanceComparator(body));
        Optional<ItemEntity> nearestVisibleLovedItem = items.stream()
                .filter(itemEntity -> body.wantsToPickUp(level, itemEntity.getItem()))
                .filter(itemEntity -> itemEntity.closerThan(body, 32.0))
                .filter(body::hasLineOfSight)
                .findFirst();
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, nearestVisibleLovedItem);
    }
}
