package io.github.wasabithumb.yandisk4j.except;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The request was successful, but the backend raised an issue.
 */
public final class YanDiskAPIException extends YanDiskException {

    @ApiStatus.Internal
    public static @NotNull YanDiskAPIException fromJSON(@NotNull JsonObject object) throws IllegalArgumentException {
        String description = JsonUtil.getOptionalStringProperty(object, "error_description");
        if (description == null) description = JsonUtil.getOptionalStringProperty(object, "description");
        return new YanDiskAPIException(
                JsonUtil.getStringProperty(object, "error"),
                description
        );
    }

    //

    private final String errorCode;
    public YanDiskAPIException(@NotNull String errorCode, @Nullable String errorDescription) {
        super(Objects.requireNonNullElse(errorDescription, "Unknown Yandex API error (" + errorCode + ")"));
        this.errorCode = errorCode;
    }

    public @NotNull String errorCode() {
        return this.errorCode;
    }

    public @NotNull String errorDescription() {
        return super.getMessage();
    }

}
