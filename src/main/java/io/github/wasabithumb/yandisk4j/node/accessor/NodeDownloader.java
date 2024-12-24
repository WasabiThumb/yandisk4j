package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.transfer.Transfer;
import io.github.wasabithumb.yandisk4j.transfer.TransferService;
import io.github.wasabithumb.yandisk4j.util.StreamUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

public final class NodeDownloader extends AbstractNodeAccessor {

    public static @NotNull NodeDownloader fromJson(@NotNull JsonObject json) {
        return AbstractNodeAccessor.fromJson(json, NodeDownloader::new);
    }

    private static final TransferService TRANSFER_SERVICE = new TransferService("Download");

    public NodeDownloader(@NotNull String href, @NotNull String method) {
        super(href, method);
    }

    //

    /**
     * Starts reading the download stream.
     */
    public @NotNull InputStream open() throws YanDiskException {
        try {
            HttpURLConnection c = this.openConnection();
            return c.getInputStream();
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to download file", e);
        }
    }

    /**
     * Starts reading the download stream.
     * @deprecated Moved to {@link #open()}
     */
    @Deprecated
    public @NotNull InputStream read() throws YanDiskException {
        return this.open();
    }

    private @NotNull Transfer readAsync(@NotNull Callable<OutputStream> os) throws YanDiskIOException {
        try {
            HttpURLConnection c = this.openConnection();
            return TRANSFER_SERVICE.submit(c::getInputStream, os, c.getContentLengthLong());
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to download file", e);
        }
    }

    /**
     * Downloads the file asynchronously, writing it to the specified output stream.
     * @throws YanDiskIOException Initial connection could not be made
     */
    public @NotNull Transfer readAsync(@NotNull OutputStream os) throws YanDiskIOException {
        return this.readAsync(() -> os);
    }

    /**
     * Reads the download stream to a file.
     * @see #readAsync(File)
     */
    public void read(@NotNull File file) throws YanDiskException {
        try (InputStream is = this.open();
             OutputStream os = new FileOutputStream(file, false)
        ) {
            StreamUtil.pipe(is, os);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to pipe download to file \"" + file.getAbsolutePath() + "\"", e);
        }
    }

    /**
     * Reads the download stream to a file asynchronously.
     * @see #read(File)
     */
    public @NotNull Transfer readAsync(@NotNull File file) throws YanDiskException {
        return this.readAsync(() -> new FileOutputStream(file, false));
    }

}
