package gr1mly4memes.parallix;

import gr1mly4memes.parallix.common.entity_parallel.EntityParallelProcessor;
import gr1mly4memes.parallix.common.mixin_support.interfaces.BeforeThreadingInitialization;
import gr1mly4memes.parallix.common.mixin_support.interfaces.MinecraftServerExtended;
import gr1mly4memes.parallix.config.NoisiumConfig;
import gr1mly4memes.parallix.init.ModGameRules;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldThreaderMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("worldthreader");

	public static final String MOD_ID = "worldthreader";
	public static final String NOISIUM_MOD_ID = "noisium";
	public static final String NOISIUM_MOD_NAME = "Noisium";
	public static final Logger NOISIUM_LOGGER = LoggerFactory.getLogger(NOISIUM_MOD_ID);
	
	public static boolean ENTITY_PARALLELISM_ENABLED = false;
	public static int ENTITY_PARALLELISM_THREADS = -1; // -1 = auto-detect

	@Override
	public void onInitialize() {
		// Initialize Noisium config
		NOISIUM_LOGGER.info("Loading {}.", NOISIUM_MOD_NAME);
		NoisiumConfig.loadFromResource(NOISIUM_LOGGER);
		NOISIUM_LOGGER.info("Noisium config: noiseChunkGenerator={}, generationShapeConfig={}, chunkSection={}, chainedBlockSource={}, useGuiGraphics={}",
				NoisiumConfig.get().noiseChunkGenerator,
				NoisiumConfig.get().generationShapeConfig,
				NoisiumConfig.get().chunkSection,
				NoisiumConfig.get().chainedBlockSource,
				NoisiumConfig.get().useGuiGraphics
		);

		ModGameRules.registerGameRules();

        ServerLevelEvents.LOAD.register((server, world) -> ((MinecraftServerExtended) server).worldthreader$onLevelAddedOrRemoved());
        ServerLevelEvents.UNLOAD.register((server, world) -> ((MinecraftServerExtended) server).worldthreader$onLevelAddedOrRemoved());

        // Initialize entity parallel processor on server start
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (ENTITY_PARALLELISM_ENABLED) {
                int threads = ENTITY_PARALLELISM_THREADS <= 0 
                        ? Runtime.getRuntime().availableProcessors() 
                        : ENTITY_PARALLELISM_THREADS;
                EntityParallelProcessor.setupThreadPool(threads, WorldThreaderMod.class.getClassLoader());
                LOGGER.info("Entity parallelism enabled with {} threads", threads);
            }
        });

        // Shutdown entity parallel processor on server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (ENTITY_PARALLELISM_ENABLED) {
                EntityParallelProcessor.shutdown();
                LOGGER.info("Entity parallel processor shutdown complete");
            }
        });
	}

	public static void initializeBeforeThreading(MinecraftServer server) {
		ModGameRules.syncDebugFlag(server);
		((BeforeThreadingInitialization) Blocks.CARVED_PUMPKIN).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.END_PORTAL_FRAME).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.WITHER_SKELETON_SKULL).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.REDSTONE_TORCH).worldthreader$initBeforeThreading(server);
	}
}
