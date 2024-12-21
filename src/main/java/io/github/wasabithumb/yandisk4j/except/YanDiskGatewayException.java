package io.github.wasabithumb.yandisk4j.except;

import org.jetbrains.annotations.NotNull;

/**
 * The server encountered an unexpected error. API recommends that the request be re-made in this case.
 */
public final class YanDiskGatewayException extends YanDiskException {

    public YanDiskGatewayException(@NotNull String message) {
        super(message);
    }

    public YanDiskGatewayException(int code) {
        super("Unexpected server error (HTTP " + code + ")");
    }

    @Override
    public @NotNull String getMessage() {
        return super.getMessage();
    }

}
