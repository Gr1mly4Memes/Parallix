package gr1mly4memes.parallix.mixin.entity_parallel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Entity safety mixin for parallel entity processing.
 * Wraps critical entity operations in synchronized(this) to prevent race conditions
 * when entities from different chunks interact (e.g., riding).
 */
@Mixin(Entity.class)
public abstract class EntityParallelEntityMixin {

    @WrapMethod(method = "setRemoved")
    private void parallelSetRemoved(Entity.RemovalReason reason, Operation<Void> original) {
        synchronized (this) {
            original.call(reason);
        }
    }

    @WrapMethod(method = "addPassenger")
    private void parallelAddPassenger(Entity passenger, Operation<Void> original) {
        synchronized (this) {
            original.call(passenger);
        }
    }

    @WrapMethod(method = "removePassenger")
    private void parallelRemovePassenger(Entity passenger, Operation<Void> original) {
        synchronized (this) {
            original.call(passenger);
        }
    }

    @WrapMethod(method = "ejectPassengers")
    private void parallelEjectPassengers(Operation<Void> original) {
        synchronized (this) {
            original.call();
        }
    }

    @WrapMethod(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z")
    private boolean parallelStartRiding(Entity entityToRide, boolean force, boolean sendEventAndTriggers, Operation<Boolean> original) {
        synchronized (this) {
            return original.call(entityToRide, force, sendEventAndTriggers);
        }
    }

    @WrapMethod(method = "removeVehicle")
    private void parallelRemoveVehicle(Operation<Void> original) {
        synchronized (this) {
            original.call();
        }
    }
}
