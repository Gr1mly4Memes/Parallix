package gr1mly4memes.parallix.launcher.install;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;

public record FileDownloader(String url, String target, String hash) implements Supplier<Path> {

    @Override
    public Path get() {
        try {
            Path path = Paths.get(target);
            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.delete(path);
            }
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    throw new FileAlreadyExistsException(target);
                } else {
                    if (Util.hash(path).equalsIgnoreCase(hash)) return path;
                    else Files.delete(path);
                }
            }
            if (!Files.exists(path) && path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            var tmp = Paths.get(target + ".tmp");
            try (InputStream stream = read(url)) {
                Files.copy(stream, tmp, StandardCopyOption.REPLACE_EXISTING);
            } catch (SocketTimeoutException | SSLException e) {
                throw new RuntimeException("Timeout " + url);
            }
            if (Files.exists(tmp)) {
                String hash = Util.hash(tmp);
                if (hash.equalsIgnoreCase(this.hash)) {
                    Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
                    return path;
                } else {
                    Files.delete(tmp);
                    throw new RuntimeException("Hash not match, expect %s found %s in %s".formatted(this.hash, hash, url));
                }
            } else {
                throw new RuntimeException("Not found " + url);
            }
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Access denied for file " + e.getFile(), e);
        } catch (Exception e) {
            Util.throwException(e);
            return null;
        }
    }

    static InputStream read(String urlString) throws IOException {
        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            return redirect(url);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL syntax: " + urlString, e);
        }
    }

    private static InputStream redirect(URL url) throws IOException {
        return redirect(url, new HashSet<>());
    }

    private static InputStream redirect(URL url, Set<String> history) throws IOException {
        if (history.contains(url.toString())) {
            StringJoiner joiner = new StringJoiner("\n        ");
            joiner.add("");
            history.forEach(joiner::add);
            throw new RuntimeException("Redirect error " + joiner);
        } else {
            history.add(url.toString());
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        } else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            String location = URLDecoder.decode(connection.getHeaderField("Location"), StandardCharsets.UTF_8);
            try {
                URI baseUri = url.toURI();
                URI resolvedUri = baseUri.resolve(location);
                URL resolvedUrl = resolvedUri.toURL();
                return redirect(resolvedUrl);
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI syntax during redirect: " + location, e);
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new RuntimeException("Not found " + url);
        } else {
            throw new RemoteException("Http " + responseCode + " " + url);
        }
    }
}
