package gr1mly4memes.parallix.mixin.entity_parallel;

import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CombatTracker mixin for thread-safe combat tracking during parallel entity ticking.
 * Based on Async's CombatTrackerMixin - uses CopyOnWriteArrayList to prevent concurrent modification.
 */
@Mixin(CombatTracker.class)
public class CombatTrackerMixin {

    @Shadow
    @Final
    @Mutable
    private List<CombatEntry> entries = new CopyOnWriteArrayList<>();
}
