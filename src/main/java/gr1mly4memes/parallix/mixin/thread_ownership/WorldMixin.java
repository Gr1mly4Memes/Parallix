package gr1mly4memes.parallix.mixin.thread_ownership;

import gr1mly4memes.parallix.common.thread.ThreadOwnedObject;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;

@Mixin(Level.class)
public abstract class WorldMixin implements ThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    private Thread thread;

    @Override
    public Thread worldthreader$getOwningThread() {
        return this.thread;
    }

    @Override
    public void worldthreader$setOwningThread(Thread thread) {
        this.thread = thread;
    }
}
