package io.github.wasabithumb.yandisk4j.auth.impl.code;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthScheme;
import io.github.wasabithumb.yandisk4j.auth.scope.AuthScope;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@ApiStatus.Internal
class CodeAuthHandler extends AbstractAuthHandler {

    protected final String redirectURI;
    public CodeAuthHandler(
            @NotNull String clientID,
            @NotNull String clientSecret,
            @NotNull Set<AuthScope> scopes,
            @Nullable String deviceID,
            @Nullable String deviceName,
            @Nullable String redirectURI,
            @Nullable String state
    ) {
        super(clientID, clientSecret, scopes, deviceID, deviceName, state);
        this.redirectURI = redirectURI;
    }

    @Override
    public @NotNull AuthScheme scheme() {
        return AuthScheme.CODE;
    }

    @Override
    protected @Nullable String getRedirectURI() {
        return this.redirectURI;
    }

}
