package gr1mly4memes.parallix.mixin.thread_ownership;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import gr1mly4memes.parallix.common.thread.ThreadOwnedObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource implements ThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    Thread mainThread;

    @Override
    public Thread worldthreader$getOwningThread() {
        return this.mainThread;
    }

    @Override
    public void worldthreader$setOwningThread(Thread thread) {
        this.mainThread = thread;
    }
}
