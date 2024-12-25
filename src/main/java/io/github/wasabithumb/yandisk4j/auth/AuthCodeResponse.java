package io.github.wasabithumb.yandisk4j.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An auth code and optional state data. Used as the output of {@link AuthHandler#awaitCode()}.
 * @since 0.2.1
 */
public record AuthCodeResponse(
        @NotNull String code,
        @Nullable String state
) { }
