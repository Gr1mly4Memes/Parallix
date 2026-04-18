package gr1mly4memes.parallix.launcher;

import gr1mly4memes.parallix.launcher.install.FabricInstaller;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ParallixLauncher {

    static void main(String[] args) {
        System.setProperty("fabric.skipMcProvider", "true");
        System.setProperty("org.jline.terminal.disableDeprecatedProviderWarning", String.valueOf(true));
        
        // Cache PID and version info to avoid repeated calls
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        String javaVersion = System.getProperty("java.version");
        String classVersion = System.getProperty("java.class.version");
        String version = getVersion();

        System.out.printf("%n%s%n%s - %s, Java(%s) %s PID: %s%n",
                "Parallix(Fabric)",
                version,
                classVersion,
                javaVersion,
                pid
        );
        System.out.print("");
        try {
            var install = fabricInstall();
            var ours = ParallixLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            var classloader = new URLClassLoader(Stream.concat(Stream.of(ours), install.getValue().stream().map(it -> {
                try {
                    return it.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            })).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
            Thread.currentThread().setContextClassLoader(classloader);
            var cl = Class.forName(install.getKey(), false, classloader);
            var handle = MethodHandles.lookup().findStatic(cl, "main", MethodType.methodType(void.class, String[].class));
            handle.invoke((Object) args);
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Fail to launch Parallix :(.");
        }
    }

    public static String getVersion() {
        return (ParallixLauncher.class.getPackage().getImplementationVersion() != null) ? ParallixLauncher.class.getPackage().getImplementationVersion() : "DEV";
    }

    private static Map.Entry<String, List<Path>> fabricInstall() throws Throwable {
        return FabricInstaller.applicationInstall();
    }
}
