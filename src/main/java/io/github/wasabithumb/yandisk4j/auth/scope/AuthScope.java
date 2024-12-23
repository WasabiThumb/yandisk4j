package io.github.wasabithumb.yandisk4j.auth.scope;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A permission to grant via the OAuth flow.
 * Each permission must also be declared in the application settings.
 * @since 0.2.0
 */
@ApiStatus.NonExtendable
public interface AuthScope {

    /**
     * Creates a new {@link AuthScope} wrapping the given token. Should match a scope defined
     * by the Yandex API. All scopes that may be used by this library normally are defined in public fields
     * ({@link #INFO}, {@link #READ}, {@link #WRITE}, {@link #APP_FOLDER}) and are members of {@link #values()}.
     */
    @Contract("_ -> new")
    static @NotNull AuthScope of(@NotNull String token) {
        final NamedAuthScope named = NamedAuthScope.BY_TOKEN.get(token);
        if (named != null) return named;
        return new CustomAuthScope(token);
    }

    /**
     * Creates an array containing all named {@link AuthScope}s
     * ({@link #INFO}, {@link #READ}, {@link #WRITE}, {@link #APP_FOLDER})
     */
    @Contract(" -> new")
    static @NotNull AuthScope @NotNull [] values() {
        return NamedAuthScope.values();
    }

    /** Access information about Yandex Disk */
    AuthScope INFO = NamedAuthScope.INFO;

    /** Read all of Yandex Disk */
    AuthScope READ = NamedAuthScope.READ;

    /** Write anywhere on Yandex Disk */
    AuthScope WRITE = NamedAuthScope.WRITE;

    /** Access the app folder on Yandex Disk */
    AuthScope APP_FOLDER = NamedAuthScope.APP_FOLDER;

    //

    @NotNull String token();

    boolean isNamed();

}
