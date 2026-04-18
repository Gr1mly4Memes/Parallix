package gr1mly4memes.parallix.launcher;

import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.util.Arguments;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParallixGameProvider extends MinecraftGameProvider {
    private Path extractedModFilePath;

    @Override
    public void initialize(FabricLauncher launcher) {
        System.setProperty("log4j2.configurationFile", "log4j2_parallix.xml");
        try {
            // Use current JAR instead of extracting separate server JAR
            this.extractedModFilePath = Paths.get(ParallixGameProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (var libraryPath : System.getProperty("parallix.classpath").split(File.pathSeparator)) {
            launcher.addToClassPath(Paths.get(libraryPath));
        }
        super.initialize(launcher);
    }

    @Override
    public Arguments getArguments() {
        Arguments arguments = super.getArguments();
        String existingModsArgument = arguments.get(Arguments.ADD_MODS);
        var builtinMods = System.getProperty("parallix.builtinMods");
        String pathSeparator = File.pathSeparator;
        var modsPath = this.extractedModFilePath.toString() + pathSeparator + builtinMods;
        if (existingModsArgument != null) {
            modsPath = existingModsArgument + pathSeparator + modsPath;
        }
        arguments.put(Arguments.ADD_MODS, modsPath);
        arguments.addExtraArg("nogui");
        return arguments;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        super.unlockClassPath(launcher);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
