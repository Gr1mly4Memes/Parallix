package gr1mly4memes.parallix.launcher.install;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import gr1mly4memes.parallix.launcher.ParallixLauncher;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FabricInstaller {

    public static Map.Entry<String, List<Path>> applicationInstall() throws Exception {
        InputStream stream = ParallixLauncher.class.getResourceAsStream("/installer.json");
        if (stream == null) {
            throw new RuntimeException("Could not find installer.json resource");
        }

        InstallInfo installInfo = new Gson().fromJson(new InputStreamReader(stream), InstallInfo.class);
        List<Supplier<Path>> suppliers = MinecraftProvider.checkMavenNoSource(installInfo.fabricDeps());
        Path path = Paths.get("libraries/net/fabricmc/fabric-loader", installInfo.installer.fabricLoader, "fabric-loader-" + installInfo.installer.fabricLoader + ".jar");

        var installFabric = !Files.exists(path) || fabricClasspathMissing(path);
        var minecraftJarMissing = minecraftJarMissing(installInfo);

        if (!suppliers.isEmpty() || installFabric || minecraftJarMissing) {
            // Count total downloads for progress bar
            int totalDownloads = suppliers.size();
            if (installFabric) totalDownloads += 1; // fabric loader
            if (installFabric && Files.exists(path)) {
                try {
                    totalDownloads += fabricDeps(path).size(); // fabric loader deps
                } catch (Exception e) {
                    // If fabric loader exists but can't read deps, estimate
                    totalDownloads += 50; // approximate
                }
            } else if (installFabric) {
                totalDownloads += 50; // approximate for new fabric loader
            }
            if (minecraftJarMissing) totalDownloads += 2; // minecraft data + server jar

            ProgressBar progressBar = new ProgressBar(totalDownloads, "Downloading libraries");
            System.out.println("Downloading missing libraries ...");

            ExecutorService pool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
            CompletableFuture<?>[] array = suppliers.stream().map(MinecraftProvider.reportSupply(pool, progressBar)).toArray(CompletableFuture[]::new);

            if (installFabric || minecraftJarMissing) {
                var futures = installFabricAndMinecraft(installInfo, pool, progressBar, minecraftJarMissing);
                array = Stream.concat(Arrays.stream(futures), Arrays.stream(array)).toArray(CompletableFuture[]::new);
            }

            MinecraftProvider.handleFutures(System.out::println, array);
            progressBar.finish();
            pool.shutdownNow();
        } else {
            System.out.println("All libraries up to date, skipping download.");
        }
        return classpath(installInfo, path);
    }

    private static boolean minecraftJarMissing(InstallInfo info) {
        var mcPath = String.format("libraries/net/minecraft/server/%1$s/server-%1$s.jar", info.installer.minecraft);
        return !Files.exists(Paths.get(mcPath));
    }

    private static final Set<String> BUILTIN_MODS = Set.of(
            "net.fabricmc.fabric-api:fabric-api:"
    );

    private static Map.Entry<String, List<Path>> classpath(InstallInfo info, Path path) throws Exception {
        var mcPath = String.format("libraries/net/minecraft/server/%1$s/server-%1$s.jar", info.installer.minecraft);
        System.setProperty("fabric.gameJarPath", Paths.get(mcPath).toAbsolutePath().toString());
        var gameLibs = info.fabricDeps().keySet().stream()
                .filter(it -> BUILTIN_MODS.stream().noneMatch(it::startsWith))
                .map(it -> "libraries/" + Util.mavenToPath(it)).collect(Collectors.joining(File.pathSeparator));
        System.setProperty("parallix.classpath", gameLibs);
        var builtinMods = info.fabricDeps().keySet().stream()
                .filter(it -> BUILTIN_MODS.stream().anyMatch(it::startsWith))
                .map(it -> "libraries/" + Util.mavenToPath(it)).collect(Collectors.joining(File.pathSeparator));
        System.setProperty("parallix.builtinMods", builtinMods);
        var libs = new ArrayList<Path>();
        fabricDeps(path).keySet().stream().map(it -> Paths.get("libraries", Util.mavenToPath(it))).forEach(libs::add);
        info.fabricDeps().keySet()
                .forEach(it -> libs.add(Paths.get("libraries", Util.mavenToPath(it))));
        libs.add(path);
        try (var file = new JarFile(path.toFile())) {
            var mainClass = file.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            return Map.entry(mainClass, libs);
        }
    }

    private static boolean fabricClasspathMissing(Path fabricLoader) throws Exception {
        return fabricDeps(fabricLoader).keySet().stream().anyMatch(it -> !Files.exists(Paths.get("libraries", Util.mavenToPath(it))));
    }

    private static Map<String, Map.Entry<String, String>> fabricDeps(Path fabricLoader) throws Exception {
        var ret = new HashMap<String, Map.Entry<String, String>>();
        try (var file = new JarFile(fabricLoader.toFile())) {
            var entry = file.getEntry("fabric-installer.json");
            try (var stream = file.getInputStream(entry)) {
                var libraries = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject().getAsJsonObject("libraries");
                for (var block : List.of(libraries.getAsJsonArray("common"), libraries.getAsJsonArray("server"))) {
                    for (var element : block) {
                        var obj = element.getAsJsonObject();
                        var name = obj.get("name").getAsString();
                        var url = obj.get("url").getAsString() + Util.mavenToPath(name);
                        var hash = obj.get("sha1").getAsString();
                        ret.put(name, new AbstractMap.SimpleImmutableEntry<>(hash, url));
                    }
                }
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<Path>[] installFabricAndMinecraft(InstallInfo info, ExecutorService pool, ProgressBar progressBar, boolean downloadMinecraft) {
        CompletableFuture<Path> minecraftDataFuture = null;

        if (downloadMinecraft) {
            var minecraftData = MinecraftProvider.downloadMinecraftData(info, pool, progressBar);
            minecraftDataFuture = minecraftData.thenCompose(data ->
                    MinecraftProvider.reportSupply(pool, progressBar).apply(
                            new FileDownloader(String.format(data.serverUrl(), info.installer.minecraft),
                                    String.format("libraries/net/minecraft/server/%1$s/server-%1$s.jar", info.installer.minecraft), data.serverHash())
                    )
            );
        }

        String coord = String.format("net.fabricmc:fabric-loader:%s", info.installer.fabricLoader);
        String dist = "libraries/" + Util.mavenToPath(coord);
        var installerFuture = MinecraftProvider.reportSupply(pool, progressBar).apply(new MavenDownloader(Mirrors.getMavenRepo(), coord, dist, info.installer.fabricLoaderHash))
                .thenAccept(path -> {
                    try {
                        var deps = fabricDeps(path);
                        var suppliers = MinecraftProvider.checkMaven(deps);
                        var array = suppliers.stream().map(MinecraftProvider.reportSupply(pool, progressBar)).toArray(CompletableFuture[]::new);
                        MinecraftProvider.handleFutures(null, array);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        if (minecraftDataFuture != null) {
            return new CompletableFuture[]{installerFuture, minecraftDataFuture};
        } else {
            return new CompletableFuture[]{installerFuture};
        }
    }
}
