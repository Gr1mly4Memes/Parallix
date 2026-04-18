package gr1mly4memes.parallix.mixin.dimension_change.arrival;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import gr1mly4memes.parallix.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends Entity implements EntityExtended {

    public ThrownEnderpearlMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void worldthreader$onArrivedInServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
        this.placePortalTicket(BlockPos.containing(this.position()));
    }
}
