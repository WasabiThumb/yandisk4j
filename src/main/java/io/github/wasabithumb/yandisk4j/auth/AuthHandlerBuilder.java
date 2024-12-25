package io.github.wasabithumb.yandisk4j.auth;

import io.github.wasabithumb.yandisk4j.auth.scope.AuthScope;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds an {@link AuthHandler}.
 * The {@link #clientID(String)} and {@link #clientSecret(String)} methods are required.
 */
public interface AuthHandlerBuilder {

    /**
     * Sets your application's client ID. This can be retrieved <a href="https://oauth.yandex.com/">here</a>.
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder clientID(@NotNull String clientID);

    /**
     * Sets your application's client secret. This can be retrieved <a href="https://oauth.yandex.com/">here</a>.
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder clientSecret(@NotNull String clientSecret);

    /**
     * Sets the scopes to request. Will override any previous calls.
     * By default, no scopes are requested (application won't be able to do anything)
     * @see #addScope(AuthScope)
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder scopes(@NotNull AuthScope @NotNull ... scopes);

    /**
     * Adds a scope.
     * @see #scopes(AuthScope...) 
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder addScope(@NotNull AuthScope scope);

    /**
     * Sets the device ID to use for this request. From Yandex API docs:
     * <p>
     *     Unique ID of the device the token is requested for.
     *     To ensure uniqueness, just generate a UUID once and use it every time a new token is
     *     requested from this device.
     *     The ID must be from 6 to 50 characters long. Only printable ASCII characters are
     *     allowed (with codes from 32 to 126).
     * </p>
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder deviceID(@Nullable String deviceID);

    /**
     * Sets the device name to use for this request. From Yandex API docs:
     * <p>
     *     The name of the device to show users. Up to 100 characters.
     *     For mobile devices, we recommend passing the device name specified by the user. If a name is missing,
     *     the name can be taken from the device model, OS name and version, and so on.
     * </p>
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder deviceName(@Nullable String deviceName);

    /**
     * Sets the redirect URI to use for this request.
     * @throws IllegalArgumentException The redirect URI may not be honored for this authentication scheme.
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder redirectURI(@Nullable String uri) throws IllegalArgumentException;

    /**
     * Sets the message to send to the user after successful authentication, if any.
     * Currently only supported for {@link AuthScheme#LOCAL_CODE}.
     * @since 0.2.1
     * @see #errorMessage(String)
     */
    @Contract("_ -> this")
    default @NotNull AuthHandlerBuilder successMessage(@NotNull String message) {
        return this;
    }

    /**
     * Sets the message to send to the user after unsuccessful authentication, if any.
     * Currently only supported for {@link AuthScheme#LOCAL_CODE}.
     * @since 0.2.1
     * @see #successMessage(String)
     */
    @Contract("_ -> this")
    default @NotNull AuthHandlerBuilder errorMessage(@NotNull String message) {
        return this;
    }

    /**
     * The state string to pass through OAuth as-is.
     * @throws IllegalArgumentException The state string is too long (more than 1024 characters)
     */
    @Contract("_ -> this")
    @NotNull AuthHandlerBuilder state(@Nullable String state) throws IllegalArgumentException;

    /**
     * Builds the {@link AuthHandler}.
     * @throws IllegalStateException A precondition for the construction of the auth handler is not satisfied
     * (for instance, {@link #clientID(String)} or {@link #clientSecret(String)} are not set).
     */
    @Contract(" -> new")
    @NotNull AuthHandler build() throws IllegalStateException;

}
