package io.github.wasabithumb.yandisk4j.auth;

/**
 * One of {@link #CODE}, {@link #SCREEN_CODE}
 */
public enum AuthScheme {
    /**
     * The OAuth flow redirects to a URL defined in the application settings, where the confirmation code must be
     * harvested and returned to the {@link AuthHandler}.
     */
    CODE,

    /**
     * The OAuth flow redirects to a constant URL where the confirmation code will be shown to the user, and
     * the application must receive this code and provide it to the {@link AuthHandler}.
     */
    SCREEN_CODE
}
