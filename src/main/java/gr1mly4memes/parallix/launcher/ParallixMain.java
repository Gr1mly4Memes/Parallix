package gr1mly4memes.parallix.launcher;

import gr1mly4memes.parallix.launcher.install.FabricInstaller;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

public class ParallixMain {

    private static final PrintStream NULL_PRINT_STREAM = new PrintStream(new java.io.OutputStream() {
        public void write(int b) {}
        public void write(byte[] b) {}
        public void write(byte[] b, int off, int len) {}
    });

    public static void main(String[] args) throws Throwable {
        System.setProperty("fabric.skipMcProvider", "true");
        System.setProperty("parallix.alwaysExtract", "true");
        try {
            var install = FabricInstaller.applicationInstall();
            var ours = ParallixMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            var classloader = new URLClassLoader(Stream.concat(Stream.of(new URL(ours)), install.getValue().stream().map(it -> {
                try {
                    return it.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            })).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
            Thread.currentThread().setContextClassLoader(classloader);

            // Redirect System.out to suppress unpacking messages from fabric-loader
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            originalOut.println("Unpacking libraries...");
            originalOut.flush();
            System.setOut(NULL_PRINT_STREAM);
            System.setErr(NULL_PRINT_STREAM);

            try {
                var cl = Class.forName(install.getKey(), false, classloader);
                var handle = MethodHandles.lookup().findStatic(cl, "main", MethodType.methodType(void.class, String[].class));
                handle.invoke((Object) args);
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fail to launch Parallix :(.");
            System.exit(-1);
        }
    }
}
