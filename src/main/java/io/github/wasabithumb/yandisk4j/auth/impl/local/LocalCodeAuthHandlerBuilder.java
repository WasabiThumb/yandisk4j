package io.github.wasabithumb.yandisk4j.auth.impl.local;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.auth.AuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthHandlerBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Objects;

@ApiStatus.Internal
public class LocalCodeAuthHandlerBuilder extends AbstractAuthHandlerBuilder {

    private static final String DEFAULT_URI = "http://127.0.0.1:8127/";

    //

    private String successMessage = "Authentication successful. You may now close this window.";
    private String errorMessage = "Failed to authenticate at this time.";
    private int port = 8127;

    @Override
    public @NotNull AuthHandlerBuilder redirectURI(@Nullable String uri) throws IllegalArgumentException {
        if (uri == null) return super.redirectURI(null);

        URI qual;
        try {
            qual = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Redirect URI \"" + uri + "\" is invalid", e);
        }

        String scheme = qual.getScheme();
        if (scheme == null || !scheme.equals("http"))
            throw new IllegalArgumentException("Scheme of redirect URI \"" + uri + "\" is not HTTP (got " + scheme + ")");

        String host = qual.getHost();
        if (host == null)
            throw new IllegalArgumentException("Redirect URI \"" + uri + "\" has no host");

        InetAddress hostAddress = null;
        Throwable hostAddressErrorCause = null;
        try {
            hostAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            hostAddressErrorCause = e;
        }
        if (hostAddress == null) {
            throw new IllegalArgumentException(
                    "Cannot resolve host address of redirect URI \"" + uri + "\"",
                    hostAddressErrorCause
            );
        }

        InetAddress localHostAddress = null;
        try {
            localHostAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) { }
        if (!hostAddress.isLoopbackAddress() && !hostAddress.equals(localHostAddress)) {
            throw new IllegalArgumentException("Host of redirect URI \"" + uri +
                    "\" is not loopback or localhost (got " + hostAddress.getHostAddress() + ")");
        }

        String path = qual.getRawPath();
        if (path != null && !path.isEmpty() && !path.equals("/"))
            throw new IllegalArgumentException("Redirect URI \"" + uri + "\" has non-root path");

        int port = qual.getPort();
        if (port == -1) port = 80;

        this.port = port;
        return super.redirectURI(uri);
    }

    @Override
    public @NotNull AuthHandlerBuilder successMessage(@NotNull String message) {
        this.successMessage = message;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder errorMessage(@NotNull String message) {
        this.errorMessage = message;
        return this;
    }

    @Override
    public @NotNull AuthHandler build() throws IllegalStateException {
        return new LocalCodeAuthHandler(
                this.assertClientID(),
                this.assertClientSecret(),
                this.assertScopes(),
                this.deviceID,
                this.deviceName,
                Objects.requireNonNullElse(this.redirectURI, DEFAULT_URI),
                this.state,
                this.port,
                this.successMessage,
                this.errorMessage
        );
    }

}
