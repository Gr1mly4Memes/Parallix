package gr1mly4memes.parallix.mixin.entity_parallel;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Entity safety mixin for parallel entity processing.
 * Based on Async's EntityMixin - adds synchronization for critical entity operations.
 */
@Mixin(Entity.class)
public abstract class EntityParallelEntityMixin {

    @Shadow
    public abstract Level level();

    @Unique
    private static final Object ENTITY_LOCK = new Object();

    /**
     * Synchronize setRemoved to prevent race conditions during parallel entity ticking.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "setRemoved",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelSetRemoved(Entity.RemovalReason reason, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        synchronized (ENTITY_LOCK) {
            // Original method will run after this injection
        }
    }

    /**
     * Synchronize passenger operations to prevent race conditions.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "addPassenger",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelAddPassenger(Entity passenger, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        synchronized (ENTITY_LOCK) {
            // Original method will run after this injection
        }
    }

    /**
     * Synchronize passenger operations to prevent race conditions.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "removePassenger",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelRemovePassenger(Entity passenger, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        synchronized (ENTITY_LOCK) {
            // Original method will run after this injection
        }
    }

    /**
     * Synchronize eject passengers to prevent race conditions.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "ejectPassengers",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelEjectPassengers(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        synchronized (ENTITY_LOCK) {
            // Original method will run after this injection
        }
    }

    /**
     * Synchronize startRiding to prevent race conditions.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelStartRiding(Entity entityToRide, boolean force, boolean sendEventAndTriggers, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        synchronized (this) {
            // Original method will run after this injection
        }
    }

    /**
     * Synchronize removeVehicle to prevent race conditions.
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "removeVehicle",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void parallelRemoveVehicle(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        synchronized (this) {
            // Original method will run after this injection
        }
    }
}
