package gr1mly4memes.parallix.common.mixin_support.interfaces;

import net.minecraft.server.MinecraftServer;

public interface BeforeThreadingInitialization {
    void worldthreader$initBeforeThreading(MinecraftServer server);
}
