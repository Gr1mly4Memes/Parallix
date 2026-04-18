package gr1mly4memes.parallix.common.mixin_support.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import gr1mly4memes.parallix.common.WorldThreaderTickPhase;
import gr1mly4memes.parallix.common.dimension_change.TeleportedEntityInfo;

import java.util.function.Consumer;

public interface ServerWorldExtended {

    void worldthreader$receiveTeleportedEntity(ResourceKey<Level> source, TeleportedEntityInfo teleportedEntityInfo);

    void worldthreader$finishReceivingTeleportedEntities(Consumer<Entity> entityAdditionalTickConsumer);

    void worldthreader$receiveFailedTeleport(TeleportedEntityInfo teleportedEntityInfo);

    void worldthreader$recoverFailedTeleports();

    TeleportedEntityInfo worldthreader$arrivingEntityInfo();

    void worldthreader$setArrivingEntityInfo(TeleportedEntityInfo teleportedEntityInfo);

    TeleportedEntityInfo worldthreader$removeDepartingEntityInfo();

    void worldthreader$putDepartingPassengerEntityInfo(TeleportedEntityInfo teleportedEntityInfo);

    WorldThreaderTickPhase worldthreader$getTickPhase();

    void worldthreader$setTickPhase(WorldThreaderTickPhase tickPhase);
}
