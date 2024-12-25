package io.github.wasabithumb.yandisk4j.auth.impl.local;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthCodeResponse;
import io.github.wasabithumb.yandisk4j.auth.AuthScheme;
import io.github.wasabithumb.yandisk4j.auth.impl.local.server.LocalCodeAuthServer;
import io.github.wasabithumb.yandisk4j.auth.scope.AuthScope;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.except.YanDiskLimitException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApiStatus.Internal
class LocalCodeAuthHandler extends AbstractAuthHandler {

    private static final String BODY_PREFIX = "<!DOCTYPE html><html><head><title>\uD83D\uDD11</title><script type=\"text/plain\">yandisk4j by WasabiThumb</script></head><body style=\"display: flex;flex-direction: column;align-items: center;justify-content: center;height: 100dvh;box-sizing: border-box;margin: 0\"><script type=\"text/plain\" style=\"display: block;font-family: monospace;font-size: 2vw;font-weight: bold\">";
    private static final String BODY_POSTFIX = "</script></body></html>";

    protected final String redirectURI;
    protected final int port;
    protected final String successMessage;
    protected final String errorMessage;
    public LocalCodeAuthHandler(
            @NotNull String clientID,
            @NotNull String clientSecret,
            @NotNull Set<AuthScope> scopes,
            @Nullable String deviceID,
            @Nullable String deviceName,
            @NotNull String redirectURI,
            @Nullable String state,
            int port,
            @NotNull String successMessage,
            @NotNull String errorMessage
    ) {
        super(clientID, clientSecret, scopes, deviceID, deviceName, state);
        this.redirectURI = redirectURI;
        this.port = port;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }

    @Override
    public @NotNull AuthScheme scheme() {
        return AuthScheme.LOCAL_CODE;
    }

    @Override
    protected @NotNull String getRedirectURI() {
        return this.redirectURI;
    }

    @Override
    public @NotNull AuthCodeResponse awaitCode() throws YanDiskException {
        final LocalCodeAuthServer server = LocalCodeAuthServer.create(this.port);
        server.setResponseParams(BODY_PREFIX, BODY_POSTFIX, this.successMessage, this.errorMessage);

        try {
            server.start();
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to start auth server", e);
        }

        AuthCodeResponse response = null;
        boolean interrupted = false;
        try {
            response = server.bind().get(10L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            interrupted = true;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof YanDiskException yde)
                throw yde;
            throw new AssertionError("Unexpected ExecutionException", e);
        } catch (TimeoutException e) {
            throw new YanDiskLimitException("Timed out waiting for auth code");
        } finally {
            server.stop();
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
            //noinspection DataFlowIssue
            return null;
        }

        return response;
    }

}
