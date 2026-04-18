package gr1mly4memes.parallix.mixin.entity_parallel;

import gr1mly4memes.parallix.common.entity_parallel.ConcurrentCollections;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

/**
 * GoalSelector thread-safety for parallel entity ticking.
 * Based on com.axalotl.async.common.mixin.entity.GoalSelectorMixin
 */
@Mixin(GoalSelector.class)
public class GoalSelectorMixin {

    @Shadow
    private final Set<WrappedGoal> availableGoals = ConcurrentCollections.newHashSet();
}
