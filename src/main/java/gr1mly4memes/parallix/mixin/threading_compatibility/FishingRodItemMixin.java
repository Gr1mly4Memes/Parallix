package gr1mly4memes.parallix.mixin.threading_compatibility;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import gr1mly4memes.parallix.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @WrapOperation(
            method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;retrieve(Lnet/minecraft/world/item/ItemStack;)I")
    )
    private int threadSafeFishingRodRetrieve(FishingHook instance, ItemStack itemStack, Operation<Integer> original) {
        if (instance.level() instanceof ServerLevel serverLevel && WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing(serverLevel)) {
            serverLevel.getServer().getAllLevels();
        }
        return original.call(instance, itemStack);
    }
}
