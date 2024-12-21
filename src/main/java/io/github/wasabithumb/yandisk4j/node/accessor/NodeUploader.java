package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskGatewayException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.except.YanDiskLimitException;
import io.github.wasabithumb.yandisk4j.util.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Handles uploading data to a file. Use {@link #write(InputStream)} to write the data.
 */
public final class NodeUploader extends AbstractNodeAccessor {

    public static @NotNull NodeUploader fromJson(@NotNull JsonObject json) {
        return AbstractNodeAccessor.fromJson(json, NodeUploader::new);
    }

    public NodeUploader(@NotNull String href, @NotNull String method) {
        super(href, method);
    }

    //

    private void unwrapResponseCode(int response) throws YanDiskException {
        if (200 <= response && response <= 299) return;
        if (response == 413)
            throw new YanDiskLimitException("File too large (HTTP 413)");
        if (response == 507)
            throw new YanDiskLimitException("Out of space (HTTP 507)");
        if (500 <= response && response <= 599)
            throw new YanDiskGatewayException(response);
        throw new YanDiskIOException(new IOException("Non-2XX HTTP response code " + response));
    }

    public @NotNull OutputStream write() throws YanDiskException {
        HttpURLConnection c;
        OutputStream os;
        try {
            c = this.openConnection();
            c.setDoOutput(true);
            os = c.getOutputStream();
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to upload file", e);
        }
        return new FilterOutputStream(os) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    NodeUploader.this.unwrapResponseCode(c.getResponseCode());
                }
            }
        };
    }

    /**
     * @see #write(File)
     * @see #write(URL)
     */
    public void write(@NotNull InputStream content) throws YanDiskException {
        int response;
        try {
            HttpURLConnection c = this.openConnection();
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                StreamUtil.pipe(content, os);
            }
            response = c.getResponseCode();
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to upload file", e);
        }
        this.unwrapResponseCode(response);
    }


    /**
     * @see #write(File)
     * @see #write(InputStream)
     */
    public void write(@NotNull URL content) throws YanDiskException {
        try {
            URLConnection c = content.openConnection();
            try (InputStream is = c.getInputStream()) {
                this.write(is);
            }
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to open URL (" + content + ")", e);
        }
    }


    /**
     * @see #write(InputStream)
     * @see #write(URL)
     */
    public void write(@NotNull File file) throws YanDiskException {
        try (FileInputStream fis = new FileInputStream(file)) {
            this.write(fis);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to read file (" + file.getAbsolutePath() + ")", e);
        }
    }

}
