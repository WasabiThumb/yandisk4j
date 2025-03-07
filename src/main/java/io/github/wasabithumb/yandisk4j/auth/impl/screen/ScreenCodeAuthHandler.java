package io.github.wasabithumb.yandisk4j.auth.impl.screen;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthScheme;
import io.github.wasabithumb.yandisk4j.auth.scope.AuthScope;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@ApiStatus.Internal
class ScreenCodeAuthHandler extends AbstractAuthHandler {

    static final String ADDRESS = "https://oauth.yandex.com/verification_code";

    public ScreenCodeAuthHandler(
            @NotNull String clientID,
            @NotNull String clientSecret,
            @NotNull Set<AuthScope> scopes,
            @Nullable String deviceID,
            @Nullable String deviceName,
            @Nullable String state
    ) {
        super(clientID, clientSecret, scopes, deviceID, deviceName, state);
    }

    @Override
    public @NotNull AuthScheme scheme() {
        return AuthScheme.SCREEN_CODE;
    }

    @Override
    protected @NotNull String getRedirectURI() {
        return ADDRESS;
    }

}
