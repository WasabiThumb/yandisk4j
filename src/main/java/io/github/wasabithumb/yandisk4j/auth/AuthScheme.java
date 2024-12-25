package io.github.wasabithumb.yandisk4j.auth;

/**
 * One of {@link #CODE}, {@link #SCREEN_CODE}
 */
public enum AuthScheme {
    /**
     * The OAuth flow redirects to a URL defined in the application settings, where the confirmation code must be
     * harvested and returned to the {@link AuthHandler}.
     * @see #SCREEN_CODE
     * @see #LOCAL_CODE
     */
    CODE,

    /**
     * The OAuth flow redirects to a constant URL where the confirmation code will be shown to the user, and
     * the application must receive this code and provide it to the {@link AuthHandler}.
     */
    SCREEN_CODE,

    /**
     * Similar to {@link #CODE}, but also opens a local HTTP server to receive the authorization code
     * via {@link AuthHandler#awaitCode()}. This requires a redirect URL to be defined in the application settings
     * with host set to a loopback address ({@code 127.0.0.1}, {@code localhost}) and a non-privileged port.
     * If no URL is specified in the {@link AuthHandlerBuilder}, the URL {@code http://127.0.0.1:8127/} is used.
     * @since 0.2.1
     */
    LOCAL_CODE
}
