package io.github.wasabithumb.yandisk4j.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ApiStatus.Internal
public final class StreamUtil {

    public static void pipe(@NotNull InputStream is, @NotNull OutputStream os, int bufSize) throws IOException {
        byte[] buf = new byte[bufSize];
        int read;
        while ((read = is.read(buf, 0, bufSize)) != -1) {
            os.write(buf, 0, read);
        }
        os.flush();
    }

    public static void pipe(@NotNull InputStream is, @NotNull OutputStream os) throws IOException {
        pipe(is, os, 8192);
    }

}
