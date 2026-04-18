package gr1mly4memes.parallix.mixin.threading_compatibility.block_behavior.patterns;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import gr1mly4memes.parallix.common.mixin_support.interfaces.BeforeThreadingInitialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EndPortalFrameBlock.class)
public abstract class EndPortalFrameBlockMixin implements BeforeThreadingInitialization {

    @Shadow
    public static native BlockPattern getOrCreatePortalShape();


    @Override
    public void worldthreader$initBeforeThreading(MinecraftServer server) {
        getOrCreatePortalShape();
    }
}
