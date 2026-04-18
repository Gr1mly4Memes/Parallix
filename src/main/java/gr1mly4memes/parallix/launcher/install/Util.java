package gr1mly4memes.parallix.launcher.install;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class Util {

    public static String mavenToPath(String maven) {
        String type;
        if (maven.matches(".*@\\w+$")) {
            int i = maven.lastIndexOf('@');
            type = maven.substring(i + 1);
            maven = maven.substring(0, i);
        } else {
            type = "jar";
        }
        String[] arr = maven.split(":");
        if (arr.length == 3) {
            String pkg = arr[0].replace('.', '/');
            return String.format("%s/%s/%s/%s-%s.%s", pkg, arr[1], arr[2], arr[1], arr[2], type);
        } else if (arr.length == 4) {
            String pkg = arr[0].replace('.', '/');
            return String.format("%s/%s/%s/%s-%s-%s.%s", pkg, arr[1], arr[2], arr[1], arr[2], arr[3], type);
        } else throw new RuntimeException("Wrong maven coordinate " + maven);
    }

    private static final String SHA_PAD = String.format("%040d", 0);
    private static final ThreadLocal<MessageDigest> SHA1_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });

    public static String hash(String path) throws Exception {
        return hash(Paths.get(path));
    }

    public static String hash(File path) throws Exception {
        return hash(path.toPath());
    }

    public static String hash(Path path) throws Exception {
        MessageDigest digest = SHA1_DIGEST.get();
        digest.reset();
        String hash = new BigInteger(1, digest.digest(Files.readAllBytes(path))).toString(16);
        return (SHA_PAD + hash).substring(hash.length());
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void throwException(Throwable e) throws E {
        throw (E) e;
    }
}
