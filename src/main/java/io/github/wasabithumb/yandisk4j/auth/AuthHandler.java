package io.github.wasabithumb.yandisk4j.auth;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.jetbrains.annotations.NotNull;

public interface AuthHandler {

    /**
     * Provides the {@link AuthScheme scheme} of this handler.
     */
    @NotNull AuthScheme scheme();

    /**
     * Provides the URL that the user must navigate to in order to complete the authentication.
     */
    @NotNull String getURL();

    /**
     * Opens the OAuth URL (as specified by {@link #getURL()}) in the system's default web browser.
     */
    void openURL();

    /**
     * Exchanges the authorization code provided by the Yandex API for an OAuth token.
     */
    @NotNull AuthResponse exchange(@NotNull String code) throws YanDiskException;

}
