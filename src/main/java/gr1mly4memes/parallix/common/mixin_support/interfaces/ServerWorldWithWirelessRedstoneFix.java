package gr1mly4memes.parallix.common.mixin_support.interfaces;

import java.util.concurrent.atomic.AtomicInteger;

public interface ServerWorldWithWirelessRedstoneFix {

    int worldthreader$getNextEntityId(AtomicInteger entityIdCounter);
}
