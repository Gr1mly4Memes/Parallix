package gr1mly4memes.parallix.mixin.entity_parallel;

import gr1mly4memes.parallix.common.entity_parallel.ConcurrentCollections;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Area effect cloud victim tracking thread-safety for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.AreaEffectCloudMixin
 */
@Mixin(AreaEffectCloud.class)
public class AreaEffectCloudMixin {
    @Shadow
    @Final
    @Mutable
    private Map<Entity, Integer> victims;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void makeCollectionsThreadSafe(EntityType<? extends AreaEffectCloud> type, Level level, CallbackInfo ci) {
        this.victims = ConcurrentCollections.newHashMap();
    }
}
