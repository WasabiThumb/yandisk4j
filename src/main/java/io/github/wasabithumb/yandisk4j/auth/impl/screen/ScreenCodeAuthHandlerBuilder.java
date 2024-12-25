package io.github.wasabithumb.yandisk4j.auth.impl.screen;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.auth.AuthHandler;
import io.github.wasabithumb.yandisk4j.auth.AuthHandlerBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ScreenCodeAuthHandlerBuilder extends AbstractAuthHandlerBuilder {

    @Override
    public @NotNull AuthHandlerBuilder redirectURI(@Nullable String uri) throws IllegalArgumentException {
        if (uri != null && !uri.equals(ScreenCodeAuthHandler.ADDRESS))
            throw new IllegalArgumentException("SCREEN_CODE auth scheme must have constant redirect url (" + ScreenCodeAuthHandler.ADDRESS+ ")");
        return super.redirectURI(uri);
    }

    @Override
    public @NotNull AuthHandler build() throws IllegalStateException {
        return new ScreenCodeAuthHandler(
                this.assertClientID(),
                this.assertClientSecret(),
                this.assertScopes(),
                this.deviceID,
                this.deviceName,
                this.state
        );
    }

}
