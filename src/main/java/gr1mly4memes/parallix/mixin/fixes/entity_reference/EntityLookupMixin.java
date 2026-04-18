package gr1mly4memes.parallix.mixin.fixes.entity_reference;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import gr1mly4memes.parallix.common.mixin_support.interfaces.EntityLookupExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(EntityLookup.class)
public class EntityLookupMixin<T extends EntityAccess> implements EntityLookupExtended {

    @Shadow
    @Final
    private Map<UUID, T> byUuid;

    @Override
    public Set<UUID> worldthreader$copyUUIDSet() {
        return new ObjectOpenHashSet<>(this.byUuid.keySet());
    }
}
