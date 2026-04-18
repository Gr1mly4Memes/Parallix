package gr1mly4memes.parallix.mixin.entity_parallel;

import gr1mly4memes.parallix.common.entity_parallel.ConcurrentCollections;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

/**
 * Gossip container thread-safety for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.GossipContainerMixin
 */
@Mixin(GossipContainer.class)
public class GossipContainerMixin {

    @Shadow
    private final Map<UUID, ?> gossips = ConcurrentCollections.newHashMap();
}
