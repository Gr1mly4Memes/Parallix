package gr1mly4memes.parallix.mixin.entity_parallel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Bee hive entry synchronization for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.BeeMixin
 */
@Mixin(Bee.class)
public class BeeMixin {
    @Unique
    private final Object lock = new Object();

    @WrapMethod(method = "wantsToEnterHive")
    private boolean wantsToEnterHive(Operation<Boolean> original) {
        synchronized (lock) {
            return original.call();
        }
    }
}
