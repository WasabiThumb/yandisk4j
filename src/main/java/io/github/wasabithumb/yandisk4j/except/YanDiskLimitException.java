package io.github.wasabithumb.yandisk4j.except;

import org.jetbrains.annotations.NotNull;

/**
 * Ran into a limit during the operation (for example, out of space).
 */
public final class YanDiskLimitException extends YanDiskException {

    public YanDiskLimitException(@NotNull String message) {
        super(message);
    }

    @Override
    public @NotNull String getMessage() {
        return super.getMessage();
    }

}
