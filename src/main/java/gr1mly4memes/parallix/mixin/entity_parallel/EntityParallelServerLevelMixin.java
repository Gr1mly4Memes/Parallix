package gr1mly4memes.parallix.mixin.entity_parallel;

import gr1mly4memes.parallix.WorldThreaderMod;
import gr1mly4memes.parallix.common.entity_parallel.EntityParallelProcessor;
import gr1mly4memes.parallix.common.mixin_support.interfaces.MinecraftServerExtended;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Mixin to enable parallel entity processing in ServerLevel.tick().
 * Based on Async's ServerLevelMixin approach.
 */
@Mixin(ServerLevel.class)
public abstract class EntityParallelServerLevelMixin {

    @Shadow
    @org.spongepowered.asm.mixin.Final
    public EntityTickList entityTickList;

    /**
     * Redirect the EntityTickList.forEach call to use parallel processing.
     * This is called during ServerLevel.tick() to process all entities.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void redirectEntityTicking(EntityTickList entityTickList, Consumer<Entity> consumer) {
        ServerLevel level = (ServerLevel) (Object) this;

        // Check if entity parallelism is enabled and we're in multithreaded mode
        boolean isMultithreaded = ((MinecraftServerExtended) level.getServer()).worldthreader$isTickMultithreaded();
        boolean entityParallelEnabled = WorldThreaderMod.ENTITY_PARALLELISM_ENABLED;

        if (!isMultithreaded || !entityParallelEnabled) {
            // Use vanilla sequential ticking
            entityTickList.forEach(consumer);
            return;
        }

        // Build list of entities to tick
        List<Entity> toTick = new ArrayList<>();
        entityTickList.forEach(entity -> {
            if (entity == null || entity.isRemoved()) return;

            // Skip frozen entities
            if (level.tickRateManager().isEntityFrozen(entity)) return;

            // Skip entities outside ticking range
            if (!level.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(entity.chunkPosition().pack())) return;

            // Handle passengers
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                if (!vehicle.isRemoved() && vehicle.hasPassenger(entity)) return;
                entity.stopRiding();
            }

            toTick.add(entity);
        });

        // Process entities in parallel
        EntityParallelProcessor.callEntityTickBatch(level, toTick);
    }
}
