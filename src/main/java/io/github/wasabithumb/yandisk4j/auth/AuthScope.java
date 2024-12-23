package io.github.wasabithumb.yandisk4j.auth;

import org.jetbrains.annotations.NotNull;

/**
 * A permission to grant via the OAuth flow.
 * Each permission must also be declared in the application settings.
 * @deprecated Moved to {@link io.github.wasabithumb.yandisk4j.auth.scope.AuthScope}
 */
@Deprecated
public enum AuthScope implements io.github.wasabithumb.yandisk4j.auth.scope.AuthScope {
    /**
     * @deprecated Moved to {@link io.github.wasabithumb.yandisk4j.auth.scope.AuthScope#INFO}
     */
    @Deprecated
    INFO("cloud_api:disk.info"),

    /**
     * @deprecated Moved to {@link io.github.wasabithumb.yandisk4j.auth.scope.AuthScope#READ}
     */
    @Deprecated
    READ("cloud_api:disk.read"),

    /**
     * @deprecated Moved to {@link io.github.wasabithumb.yandisk4j.auth.scope.AuthScope#WRITE}
     */
    @Deprecated
    WRITE("cloud_api:disk.write"),

    /**
     * @deprecated Moved to {@link io.github.wasabithumb.yandisk4j.auth.scope.AuthScope#APP_FOLDER}
     */
    @Deprecated
    APP_FOLDER("cloud_api:disk.app_folder");

    private final String token;
    AuthScope(@NotNull String token) {
        this.token = token;
    }

    @Override
    public @NotNull String token() {
        return this.token;
    }

    @Override
    public boolean isNamed() {
        return false;
    }

    @Override
    public @NotNull String toString() {
        return this.token;
    }

}
