package io.github.wasabithumb.yandisk4j.except;

import org.jetbrains.annotations.NotNull;

/**
 * The superclass of exceptions that may be thrown by YanDisk methods which access the Yandex API.
 */
public sealed abstract class YanDiskException
        extends RuntimeException
        permits YanDiskAPIException, YanDiskGatewayException, YanDiskIOException, YanDiskLimitException, YanDiskOperationException
{

    public YanDiskException(@NotNull String message) {
        super(message);
    }

    public YanDiskException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

    public YanDiskException(@NotNull Throwable cause) {
        super(cause);
    }

}
