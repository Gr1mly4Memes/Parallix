package gr1mly4memes.parallix.common.entity_parallel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Parallel entity processor based on Async's ParallelProcessor.
 * Integrates with Parallix's dimension threading model.
 */
public class EntityParallelProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityParallelProcessor.class);

    // Entity types that must be ticked synchronously due to thread-safety issues
    private static final Set<Class<?>> BLOCKED_ENTITIES = Set.of(
            FallingBlockEntity.class,
            Shulker.class,
            AbstractBoat.class,
            Boat.class
    );

    // Blacklisted specific entities (runtime configurable)
    private static final Set<UUID> BLACKLISTED_ENTITIES = ConcurrentHashMap.newKeySet();

    // Thread pool for entity parallelism within a dimension
    private static volatile ExecutorService executor;
    private static final AtomicInteger THREAD_POOL_ID = new AtomicInteger();
    private static volatile boolean isShuttingDown = false;

    /**
     * Initialize the entity parallel processing thread pool.
     * @param parallelism Number of threads to use
     * @param classLoader ClassLoader for the threads
     */
    public static void setupThreadPool(int parallelism, ClassLoader classLoader) {
        if (executor != null && !executor.isShutdown()) {
            return; // Already initialized
        }

        isShuttingDown = false;

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "Parallix-Entity-Pool-" + THREAD_POOL_ID.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setContextClassLoader(classLoader);
            return thread;
        };

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                parallelism,
                parallelism,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory
        );
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        pool.allowCoreThreadTimeOut(false);
        pool.prestartAllCoreThreads();

        executor = pool;
        LOGGER.info("Initialized Entity Parallel Processor with {} threads", parallelism);
    }

    /**
     * Process a batch of entities in parallel.
     * @param world The ServerLevel
     * @param entities List of entities to tick
     */
    public static void callEntityTickBatch(ServerLevel world, List<Entity> entities) {
        if (entities.isEmpty()) {
            return;
        }

        final int poolSize = getPoolSize();
        if (poolSize <= 1 || entities.size() < 10) {
            // Not worth parallelizing for small batches or single thread
            entities.forEach(e -> tickEntity(world, e, false));
            return;
        }

        final int chunkSize = (entities.size() + poolSize - 1) / poolSize;
        final List<Future<Void>> futures = new ArrayList<>();

        // Split entities into chunks and submit for parallel processing
        for (int i = 0; i < entities.size(); i += chunkSize) {
            final List<Entity> chunk = entities.subList(i, Math.min(i + chunkSize, entities.size()));
            Future<Void> future = executor.submit(() -> {
                for (Entity entity : chunk) {
                    if (!shouldTickSynchronously(entity)) {
                        tickEntity(world, entity, true);
                    }
                }
                return null;
            });
            futures.add(future);
        }

        // Tick entities that must be processed synchronously
        entities.stream()
                .filter(EntityParallelProcessor::shouldTickSynchronously)
                .forEach(e -> tickEntity(world, e, false));

        // Wait for all parallel tasks to complete
        waitForFutures(futures);
    }

    /**
     * Wait for all futures to complete while pumping chunk tasks.
     */
    private static void waitForFutures(List<Future<Void>> futures) {
        boolean allDone;
        do {
            allDone = futures.stream().allMatch(Future::isDone);
            if (!allDone) {
                // Pump chunk tasks to prevent deadlock
                boolean pumped = false;
                // Note: We can't access server here easily, so we just spin-wait
                if (!pumped) {
                    Thread.onSpinWait();
                }
            }
        } while (!allDone);

        // Check for exceptions
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                LOGGER.error("Error during async entity tick", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Determine if an entity should be ticked synchronously.
     * @param entity The entity to check
     * @return true if the entity should be ticked synchronously
     */
    public static boolean shouldTickSynchronously(Entity entity) {
        if (isShuttingDown || entity.level().isClientSide() || entity.portalProcess != null) {
            return true;
        }

        UUID entityId = entity.getUUID();

        return entity instanceof Projectile
                || entity instanceof ServerPlayer
                || BLOCKED_ENTITIES.contains(entity.getClass())
                || BLACKLISTED_ENTITIES.contains(entityId);
    }

    /**
     * Tick a single entity with error handling.
     * @param world The ServerLevel
     * @param entity The entity to tick
     * @param async Whether this is an async tick
     */
    private static void tickEntity(ServerLevel world, Entity entity, boolean async) {
        try {
            world.tickNonPassenger(entity);
        } catch (Exception e) {
            LOGGER.error("Error during {} tick. Entity: {}, UUID: {}",
                    async ? "async" : "sync", entity.getType(), entity.getUUID(), e);
        }
    }

    /**
     * Get the current thread pool size.
     * @return Number of threads in the pool
     */
    public static int getPoolSize() {
        if (executor instanceof ThreadPoolExecutor pool) {
            return pool.getCorePoolSize();
        }
        return 0;
    }

    /**
     * Add an entity to the blacklist.
     * @param entityId The entity UUID to blacklist
     */
    public static void blacklistEntity(UUID entityId) {
        BLACKLISTED_ENTITIES.add(entityId);
    }

    /**
     * Remove an entity from the blacklist.
     * @param entityId The entity UUID to remove from blacklist
     */
    public static void unblacklistEntity(UUID entityId) {
        BLACKLISTED_ENTITIES.remove(entityId);
    }

    /**
     * Check if an entity is blacklisted.
     * @param entityId The entity UUID to check
     * @return true if blacklisted
     */
    public static boolean isBlacklisted(UUID entityId) {
        return BLACKLISTED_ENTITIES.contains(entityId);
    }

    /**
     * Shutdown the entity parallel processor.
     */
    public static void shutdown() {
        isShuttingDown = true;

        if (executor != null) {
            LOGGER.info("Waiting for Entity Parallel Processor to shutdown...");
            executor.shutdown();
            try {
                executor.awaitTermination(60L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Interrupted while waiting for thread pool shutdown", e);
            }
        }

        BLACKLISTED_ENTITIES.clear();
    }
}
