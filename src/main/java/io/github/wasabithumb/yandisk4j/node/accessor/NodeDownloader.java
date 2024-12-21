package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.util.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;

public final class NodeDownloader extends AbstractNodeAccessor {

    public static @NotNull NodeDownloader fromJson(@NotNull JsonObject json) {
        return AbstractNodeAccessor.fromJson(json, NodeDownloader::new);
    }

    public NodeDownloader(@NotNull String href, @NotNull String method) {
        super(href, method);
    }

    //

    /**
     * @see #read(File)
     */
    public @NotNull InputStream read() throws YanDiskException {
        try {
            HttpURLConnection c = this.openConnection();
            return c.getInputStream();
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to download file", e);
        }
    }

    /**
     * @see #read()
     */
    public void read(@NotNull File file) throws YanDiskException {
        try (InputStream is = this.read();
             OutputStream os = new FileOutputStream(file, false)
        ) {
            StreamUtil.pipe(is, os);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to pipe download to file \"" + file.getAbsolutePath() + "\"", e);
        }
    }

}
