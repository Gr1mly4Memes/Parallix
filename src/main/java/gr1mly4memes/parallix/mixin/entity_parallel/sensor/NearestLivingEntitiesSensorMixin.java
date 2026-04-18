package gr1mly4memes.parallix.mixin.entity_parallel.sensor;

import gr1mly4memes.parallix.common.entity_parallel.SensorUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

/**
 * NearestLivingEntitiesSensor optimization with distance caching for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.sensor.NearestLivingEntitiesSensorMixin
 */
@Mixin(value = NearestLivingEntitySensor.class, priority = 1500)
public abstract class NearestLivingEntitiesSensorMixin<T extends LivingEntity> extends Sensor<T> {

    /**
     * @author Async (_Axa_lotL_)
     * @reason async distance cache
     */
    @Overwrite
    protected void doTick(final ServerLevel level, final T body) {
        double followRange = body.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB boundingBox = body.getBoundingBox().inflate(followRange, followRange, followRange);
        List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, boundingBox, mob -> mob != body && mob.isAlive());
        livingEntities.sort(SensorUtils.distanceComparator(body));
        Brain<?> brain = body.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, livingEntities);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(level, body, livingEntities));
    }
}
