package io.github.wasabithumb.yandisk4j.auth.scope;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
record CustomAuthScope(@NotNull String token) implements AuthScope {

    @Override
    public boolean isNamed() {
        return false;
    }

    @Override
    public @NotNull String toString() {
        return this.token;
    }

}
