package gr1mly4memes.parallix.common.entity_parallel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

public class EntityActivationRange {

    public static final int MONSTER_RANGE = 32;
    public static final int ANIMAL_RANGE = 32;
    public static final int VILLAGER_RANGE = 32;
    public static final int WATER_RANGE = 16;
    public static final int MISC_RANGE = 16;

    public static boolean checkIfActive(Entity entity, ServerLevel level) {
        if (entity instanceof ServerPlayer) return true;

        int range = getActivationRange(entity);
        if (range <= 0) return true;

        int rangeSq = range * range;
        BlockPos entityPos = entity.blockPosition();

        for (ServerPlayer player : level.players()) {
            if (entityPos.distSqr(player.blockPosition()) <= rangeSq) {
                return true;
            }
        }

        return false;
    }

    private static int getActivationRange(Entity entity) {
        if (entity instanceof AbstractVillager) return VILLAGER_RANGE;
        if (entity instanceof Monster) return MONSTER_RANGE;
        if (entity instanceof Animal) return ANIMAL_RANGE;
        if (entity instanceof WaterAnimal) return WATER_RANGE;
        if (entity instanceof Mob) return ANIMAL_RANGE;
        return MISC_RANGE;
    }
}
