package io.github.wasabithumb.yandisk4j.auth;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AuthResponse(
        @NotNull String accessToken,
        @NotNull String refreshToken,
        long expiresIn,
        @Nullable String scope
) {

    @ApiStatus.Internal
    public static @NotNull AuthResponse fromJSON(@NotNull JsonObject object) throws IllegalArgumentException {
        return new AuthResponse(
                JsonUtil.getStringProperty(object, "access_token"),
                JsonUtil.getStringProperty(object, "refresh_token"),
                JsonUtil.getLongProperty(object, "expires_in"),
                JsonUtil.getOptionalStringProperty(object, "scope")
        );
    }

    @Contract(pure = true)
    public @NotNull String tokenType() {
        return "bearer";
    }

}
