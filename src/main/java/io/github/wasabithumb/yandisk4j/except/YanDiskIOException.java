package io.github.wasabithumb.yandisk4j.except;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A generic {@link IOException} wrapped into a {@link YanDiskException} for easy catching.
 */
public final class YanDiskIOException extends YanDiskException {

    public YanDiskIOException(@NotNull String message, @NotNull IOException cause) {
        super(message, cause);
    }

    public YanDiskIOException(@NotNull IOException cause) {
        super(cause);
    }

    @Override
    public @NotNull IOException getCause() {
        return (IOException) super.getCause();
    }

}
