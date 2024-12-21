package io.github.wasabithumb.yandisk4j.except;

import org.jetbrains.annotations.NotNull;

/**
 * Details about the remote filesystem prohibits the requested operation.
 */
public final class YanDiskOperationException extends YanDiskException {

    public YanDiskOperationException(@NotNull String message) {
        super(message);
    }

    @Override
    public @NotNull String getMessage() {
        return super.getMessage();
    }

}
