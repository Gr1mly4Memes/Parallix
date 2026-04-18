package gr1mly4memes.parallix;

import com.google.common.collect.ImmutableMap;
import gr1mly4memes.parallix.compat.c2me.NoisiumC2MECompat;
import gr1mly4memes.parallix.compat.lithium.NoisiumLithiumCompat;
import gr1mly4memes.parallix.config.NoisiumConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class WorldThreaderMixinPlugin implements IMixinConfigPlugin {
	private static final Supplier<Boolean> TRUE = () -> true;

	private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
			// Noise chunk generator mixin (disabled when Lithium compat is active)
			"gr1mly4memes.parallix.mixin.NoiseChunkGeneratorMixin", () -> !NoisiumLithiumCompat.isLithiumLoaded() && !NoisiumC2MECompat.isC2MELoaded() && NoisiumConfig.get().noiseChunkGenerator,
			// Lithium specific alternative (disabled when C2ME is loaded)
			"gr1mly4memes.parallix.mixin.compat.lithium.LithiumNoiseChunkGeneratorMixin", () -> NoisiumLithiumCompat.isLithiumLoaded() && !NoisiumC2MECompat.isC2MELoaded() && NoisiumConfig.get().noiseChunkGenerator
	);

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		// Check mixin-specific flags first
		if (CONDITIONS.containsKey(mixinClassName)) {
			return CONDITIONS.get(mixinClassName).get();
		}

		// Fallback to generic per-mixin toggles by class simple name
		String simple = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
		switch (simple) {
			case "GenerationShapeConfigMixin":
				return NoisiumConfig.get().generationShapeConfig;
			case "ChunkSectionMixin":
				return NoisiumConfig.get().chunkSection;
			case "ChainedBlockSourceMixin":
				return NoisiumConfig.get().chainedBlockSource;
			default:
				return TRUE.get();
		}
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
