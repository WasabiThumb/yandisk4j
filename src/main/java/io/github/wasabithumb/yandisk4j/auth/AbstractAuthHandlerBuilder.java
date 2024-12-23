package io.github.wasabithumb.yandisk4j.auth;

import io.github.wasabithumb.yandisk4j.auth.scope.AuthScope;
import io.github.wasabithumb.yandisk4j.auth.scope.AuthScopeSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

@ApiStatus.Internal
public abstract class AbstractAuthHandlerBuilder implements AuthHandlerBuilder {

    protected String clientID = null;
    protected String clientSecret = null;
    protected final Set<AuthScope> scopes = new AuthScopeSet();
    protected String deviceID = null;
    protected String deviceName = null;
    protected String redirectURI = null;
    protected String state = null;

    //

    protected final @NotNull Set<AuthScope> assertScopes() throws IllegalStateException {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("No scopes are set");
        return this.scopes;
    }

    protected final @NotNull String assertClientID() throws IllegalStateException {
        if (this.clientID == null)
            throw new IllegalStateException("Client ID is not set");
        return this.clientID;
    }

    protected final @NotNull String assertClientSecret() throws IllegalStateException {
        if (this.clientSecret == null)
            throw new IllegalStateException("Client secret is not set");
        return this.clientSecret;
    }

    //

    @Override
    public @NotNull AuthHandlerBuilder clientID(@NotNull String clientID) {
        this.clientID = clientID;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder clientSecret(@NotNull String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder scopes(@NotNull AuthScope @NotNull ... scopes) {
        this.scopes.clear();
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder addScope(@NotNull AuthScope scope) {
        this.scopes.add(scope);
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder deviceID(@Nullable String deviceID) {
        this.deviceID = deviceID;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder deviceName(@Nullable String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder redirectURI(@Nullable String uri) throws IllegalArgumentException {
        this.redirectURI = uri;
        return this;
    }

    @Override
    public @NotNull AuthHandlerBuilder state(@Nullable String state) throws IllegalArgumentException {
        if (state != null && state.length() > 1024)
            throw new IllegalArgumentException("State data is too long (" + state.length() + " chars)");
        this.state = state;
        return this;
    }

    @Override
    public abstract @NotNull AuthHandler build() throws IllegalStateException;

}
