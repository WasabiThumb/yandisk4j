package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskGatewayException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.except.YanDiskLimitException;
import io.github.wasabithumb.yandisk4j.transfer.Transfer;
import io.github.wasabithumb.yandisk4j.transfer.TransferService;
import io.github.wasabithumb.yandisk4j.util.StreamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

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

    private static final TransferService TRANSFER_SERVICE = new TransferService("Upload");

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

    /**
     * Starts uploading. Any data written to the stream before {@link OutputStream#close() close()} is sent
     * as the file content. The call to {@code close()} itself may throw {@link YanDiskException} in addition to
     * {@link IOException}.
     */
    public @NotNull OutputStream open() throws YanDiskException {
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
                    NodeUploader.this.unwrapResponseCode(c.getResponseCode());
                } finally {
                    super.close();
                }
            }
        };
    }

    /**
     * Starts uploading. Any data written to the stream before {@link OutputStream#close() close()} is sent
     * as the file content.
     * @deprecated Moved to {@link #open()}
     */
    @Deprecated
    public @NotNull OutputStream write() throws YanDiskException {
        return this.open();
    }

    /**
     * Uploads the content of an {@link InputStream}.
     * @see #writeAsync(InputStream, long)
     */
    public void write(@NotNull InputStream content) throws YanDiskException {
        try (OutputStream os = this.open()) {
            StreamUtil.pipe(content, os);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to upload file", e);
        }
    }

    /**
     * Uploads the content of an {@link InputStream} asynchronously.
     * @param size Expected size of data to transfer, or -1.
     * @see #write(InputStream)
     */
    public @NotNull Transfer writeAsync(
            @NotNull InputStream content,
            @Range(from = -1L, to = Long.MAX_VALUE) long size
    ) {
        return TRANSFER_SERVICE.submit(() -> content, this::open, size);
    }

    /**
     * Uploads the content of an {@link InputStream} asynchronously. Alias for {@code writeAsync(content, -1L)}
     * @see #writeAsync(InputStream, long) 
     * @see #write(InputStream)
     */
    public @NotNull Transfer writeAsync(
            @NotNull InputStream content
    ) {
        return TRANSFER_SERVICE.submit(() -> content, this::open);
    }


    /**
     * Uploads the content of a {@link URL}.
     * @see #writeAsync(URL)
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
     * Uploads the content of a {@link URL} asynchronously.
     * @see #write(URL)
     * @throws YanDiskIOException Initial call to {@link URL#openConnection()} failed
     */
    public @NotNull Transfer writeAsync(@NotNull URL content) throws YanDiskIOException {
        try {
            URLConnection c = content.openConnection();
            return TRANSFER_SERVICE.submit(c::getInputStream, this::open, c.getContentLengthLong());
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to open URL (" + content + ")", e);
        }
    }


    /**
     * Uploads the content of a {@link File}.
     * @see #writeAsync(File)
     */
    public void write(@NotNull File file) throws YanDiskException {
        try (FileInputStream fis = new FileInputStream(file)) {
            this.write(fis);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to read file (" + file.getAbsolutePath() + ")", e);
        }
    }

    /**
     * Uploads the content of a {@link File} asynchronously.
     * @see #write(File)
     */
    public @NotNull Transfer writeAsync(@NotNull File file) {
        return TRANSFER_SERVICE.submit(() -> new FileInputStream(file), this::open, file.length());
    }

}
