package gr1mly4memes.parallix.launcher.install;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MinecraftProvider {
    static Function<Supplier<Path>, CompletableFuture<Path>> reportSupply(ExecutorService service, ProgressBar progressBar) {
        return it -> CompletableFuture.supplyAsync(it, service).thenApply(path -> {
            if (progressBar != null) {
                progressBar.update();
            }
            return path;
        });
    }

    static CompletableFuture<MinecraftData> downloadMinecraftData(InstallInfo info, ExecutorService pool, ProgressBar progressBar) {
        return CompletableFuture.supplyAsync(() -> {
            for (Map.Entry<String, String> entry : Mirrors.getVersionManifest()) {
                try (var stream = FileDownloader.read(entry.getValue())) {
                    var bytes = stream.readAllBytes();
                    JsonObject element = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                    var versions = element.getAsJsonArray("versions");
                    for (var version : versions) {
                        var id = version.getAsJsonObject().get("id").getAsString();
                        if (Objects.equals(id, info.installer.minecraft)) {
                            var url = version.getAsJsonObject().get("url").getAsString();
                            try (var versionStream = FileDownloader.read(url)) {
                                JsonObject object = JsonParser.parseString(new String(versionStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                                var downloads = object.getAsJsonObject("downloads");
                                var server = downloads.getAsJsonObject("server");
                                var serverUrl = server.get("url").getAsString();
                                var serverHash = server.get("sha1").getAsString();
                                if (progressBar != null) {
                                    progressBar.update();
                                }
                                return new MinecraftData(entry.getKey(),
                                        Mirrors.mapMojangMirror(serverUrl, entry.getKey()), serverHash);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore and try next mirror
                }
            }
            throw new RuntimeException("Failed to download Minecraft version manifest");
        }, pool);
    }

    static void handleFutures(Consumer<String> logger, CompletableFuture<?>... futures) {
        for (CompletableFuture<?> future : futures) {
            try {
                future.join();
            } catch (CompletionException e) {
                if (logger != null) {
                    logger.accept(e.getCause().toString());
                } else {
                    e.getCause().printStackTrace();
                }
                Util.throwException(e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    static List<Supplier<Path>> checkMavenNoSource(Map<String, String> map) {
        LinkedHashMap<String, Map.Entry<String, String>> hashMap = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            hashMap.put(entry.getKey(), new AbstractMap.SimpleImmutableEntry<>(entry.getValue(), null));
        }
        return checkMaven(hashMap);
    }

    static List<Supplier<Path>> checkMaven(Map<String, Map.Entry<String, String>> map) {
        List<Supplier<Path>> incomplete = new ArrayList<>(map.size());
        for (Map.Entry<String, Map.Entry<String, String>> entry : map.entrySet()) {
            String maven = entry.getKey();
            String hash = entry.getValue().getKey();
            String url = entry.getValue().getValue();
            String path = "libraries/" + Util.mavenToPath(maven);
            Path filePath = Paths.get(path);
            if (Files.exists(filePath)) {
                try {
                    String fileHash = Util.hash(filePath);
                    if (!fileHash.equals(hash)) {
                        incomplete.add(new MavenDownloader(Mirrors.getMavenRepo(), maven, path, hash, url));
                    }
                } catch (Exception e) {
                    incomplete.add(new MavenDownloader(Mirrors.getMavenRepo(), maven, path, hash, url));
                }
            } else {
                incomplete.add(new MavenDownloader(Mirrors.getMavenRepo(), maven, path, hash, url));
            }
        }
        return incomplete;
    }

    record MinecraftData(String mirror, String serverUrl, String serverHash) {
    }
}
