package gr1mly4memes.parallix.mixin.hopper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Shadow
    private int cooldownTime;

    @Unique
    private static final int MAX_COOLDOWN = 8;

    @Inject(method = "tryMoveItems", at = @At("TAIL"))
    private static void cooldownOnFull(Level level, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier validator, CallbackInfoReturnable<Boolean> cir) {
        HopperBlockEntityMixin self = (HopperBlockEntityMixin)(Object)blockEntity;
        if (self.cooldownTime == 0 && !((Container)blockEntity).isEmpty()) {
            self.cooldownTime = MAX_COOLDOWN;
        }
    }
}
