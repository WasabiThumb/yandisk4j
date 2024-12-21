package io.github.wasabithumb.yandisk4j.auth.code;

import io.github.wasabithumb.yandisk4j.auth.AbstractAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.auth.AuthHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class CodeAuthHandlerBuilder extends AbstractAuthHandlerBuilder {

    @Override
    public @NotNull AuthHandler build() throws IllegalStateException {
        return new CodeAuthHandler(
                this.assertClientID(),
                this.assertClientSecret(),
                this.assertScopes(),
                this.deviceID,
                this.deviceName,
                this.redirectURI,
                this.state
        );
    }

}
