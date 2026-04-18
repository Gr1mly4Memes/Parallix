# Parallix

Parallix is a stable Fabric server that is more vanilla than Paper and its forks, designed to provide significant performance improvements through parallel processing while maintaining a vanilla-like experience.

## Features

### Performance Optimizations
- **Parallel World Ticking** - Processes multiple worlds simultaneously using dedicated threads
- **Parallel Entity Ticking** - Processes entities in parallel threads to reduce main thread load
- **Thread-Safe Entity Systems** - entity-related mixins for safe parallel entity processing
- **AI Optimization** - Goal selectors, sensors, and brain systems optimized for concurrent access
- **Breeding Safety** - Atomic flags and synchronization for animal breeding operations
- **Equipment Safety** - Thread-safe equipment and item pickup for mobs

### Integrated Optimizations
- **WorldThreader** - Parallel world generation and chunk processing
- **Noisium Forked** - Noise-based chunk generation optimization
- **Async** - Entity parallelism with thread-safe entity mixins

### Vanilla-Like Experience
- Maintains vanilla game behavior while improving performance
- Performance features can be toggled via gamerule
- No gameplay-breaking changes

## Credits
- [Fabric API](https://github.com/FabricMC/fabric-api)
- [WorldThreader](https://github.com/2No2Name/worldthreader)
- [Noisium Forked](https://github.com/coredex-source/noisium-forked)
- [Async](https://github.com/AxalotLDev/Async)
