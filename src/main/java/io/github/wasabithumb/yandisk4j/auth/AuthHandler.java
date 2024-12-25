package io.github.wasabithumb.yandisk4j.auth;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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
     * Waits for the authorization code. The authorization code is not itself the OAuth token; the latter is
     * obtained through {@link #exchange(String)}.
     * Currently only supported for {@link AuthScheme#LOCAL_CODE}.
     * @throws UnsupportedOperationException The auth handler cannot retrieve the authorization code automatically.
     * @throws io.github.wasabithumb.yandisk4j.except.YanDiskIOException Generic IO exception
     * @throws io.github.wasabithumb.yandisk4j.except.YanDiskAPIException Authorization code could not be granted
     * @throws io.github.wasabithumb.yandisk4j.except.YanDiskLimitException Waited for too long, the code has expired (10 minutes)
     * @since 0.2.1
     */
    default @NotNull AuthCodeResponse awaitCode() throws UnsupportedOperationException, YanDiskException {
        throw new UnsupportedOperationException("AuthHandler does not support awaitCode()");
    }

    /**
     * Exchanges the authorization code provided by the Yandex API for an OAuth token.
     */
    @NotNull AuthResponse exchange(@NotNull String code) throws YanDiskException;

}
