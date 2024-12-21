package io.github.wasabithumb.yandisk4j.auth;

import org.jetbrains.annotations.NotNull;

/**
 * A permission to grant via the OAuth flow.
 * Each permission must also be declared in the application settings.
 */
public enum AuthScope {
    /** Access information about Yandex Disk */
    INFO("cloud_api:disk.info"),

    /** Read all of Yandex Disk */
    READ("cloud_api:disk.read"),

    /** Write anywhere on Yandex Disk */
    WRITE("cloud_api:disk.write"),

    /** Access the app folder on Yandex Disk */
    APP_FOLDER("cloud_api:disk.app_folder");

    private final String token;
    AuthScope(@NotNull String token) {
        this.token = token;
    }

    public @NotNull String token() {
        return this.token;
    }

    @Override
    public @NotNull String toString() {
        return this.token;
    }

}
