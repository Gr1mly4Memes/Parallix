package gr1mly4memes.parallix.mixin.threading_compatibility.block_behavior.patterns;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import gr1mly4memes.parallix.common.mixin_support.interfaces.BeforeThreadingInitialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WitherSkullBlock.class)
public abstract class WitherSkullBlockMixin implements BeforeThreadingInitialization {

    @Shadow
    public static native BlockPattern getOrCreateWitherBase();

    @Shadow
    public static native BlockPattern getOrCreateWitherFull();


    @Override
    public void worldthreader$initBeforeThreading(MinecraftServer server) {
        getOrCreateWitherBase();
        getOrCreateWitherFull();
    }
}
