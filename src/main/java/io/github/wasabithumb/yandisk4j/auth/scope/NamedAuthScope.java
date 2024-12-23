package io.github.wasabithumb.yandisk4j.auth.scope;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
enum NamedAuthScope implements AuthScope {
    INFO("cloud_api:disk.info"),
    READ("cloud_api:disk.read"),
    WRITE("cloud_api:disk.write"),
    APP_FOLDER("cloud_api:disk.app_folder");

    static final Map<String, NamedAuthScope> BY_TOKEN = new HashMap<>(4);
    static {
        for (NamedAuthScope nas : values()) BY_TOKEN.put(nas.token, nas);
    }

    private final String token;
    NamedAuthScope(@NotNull String token) {
        this.token = token;
    }

    @Override
    public @NotNull String token() {
        return this.token;
    }

    @Override
    public boolean isNamed() {
        return true;
    }

    @Override
    public @NotNull String toString() {
        return this.token;
    }

}
