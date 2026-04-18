package gr1mly4memes.parallix.common.mixin_support.interfaces;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayerInstanceSwapper {
    ServerPlayer worldthreader$swapRemovedPlayerWithNewCopy(ServerPlayer previous, ServerLevel newLevel);
}
