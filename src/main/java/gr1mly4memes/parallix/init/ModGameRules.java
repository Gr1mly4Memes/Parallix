package gr1mly4memes.parallix.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import gr1mly4memes.parallix.WorldThreaderMod;
import gr1mly4memes.parallix.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.NotNull;

public class ModGameRules {

    public static GameRule<@NotNull Boolean> ACTIVE;
	public static final boolean INITIAL_TRUE = true;
	public static final boolean INITIAL_FALSE = false;
    public static GameRule<@NotNull Boolean> TELEPORTED_ENTITY_ADDITIONAL_TICK;
    public static GameRule<@NotNull Boolean> DEBUG;
    public static GameRule<@NotNull Boolean> ENTITY_PARALLELISM;

	public static void registerGameRules() {
        ACTIVE = GameRuleBuilder.forBoolean(INITIAL_TRUE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "active"));
        TELEPORTED_ENTITY_ADDITIONAL_TICK = GameRuleBuilder.forBoolean(INITIAL_FALSE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "additional_entity_tick_after_teleport"));
        DEBUG = GameRuleBuilder.forBoolean(INITIAL_FALSE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "debug"));
        ENTITY_PARALLELISM = GameRuleBuilder.forBoolean(INITIAL_FALSE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "entity_parallelism"));
	}

	public static void syncDebugFlag(MinecraftServer server) {
		if (DEBUG != null) {
			WorldThreadingManager.DEBUG = server.getGameRules().get(DEBUG);
		}
		if (ENTITY_PARALLELISM != null) {
			WorldThreaderMod.ENTITY_PARALLELISM_ENABLED = server.getGameRules().get(ENTITY_PARALLELISM);
		}
	}
}
