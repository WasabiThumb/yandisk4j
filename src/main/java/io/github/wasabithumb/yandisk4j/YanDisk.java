package io.github.wasabithumb.yandisk4j;

import io.github.wasabithumb.yandisk4j.auth.*;
import io.github.wasabithumb.yandisk4j.auth.impl.code.CodeAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.auth.impl.local.LocalCodeAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.auth.impl.screen.ScreenCodeAuthHandlerBuilder;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.node.Node;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeDownloader;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeUploader;
import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import io.github.wasabithumb.yandisk4j.operation.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Entry point for {@code yandisk4j}
 * @see #yanDisk(Supplier)
 * @see #yanDisk(String)
 */
public final class YanDisk implements IYanDisk {

    /**
     * <p>
     *     Provides an {@link AuthHandlerBuilder} to build an {@link io.github.wasabithumb.yandisk4j.auth.AuthHandler AuthHandler}
     *     conforming to the specified {@link AuthScheme}.
     * </p>
     * <h2>Example</h2>
     * <pre>{@code
     * AuthHandler h = YanDisk.auth(AuthScheme.SCREEN_CODE)
     *      .clientID("YOUR_CLIENT_ID")
     *      .clientSecret("YOUR_CLIENT_SECRET")
     *      .scopes(AuthScope.INFO, AuthScope.READ)
     *      .build();
     *
     * h.openURL();
     * String code = promptUserForAuthCode();
     *
     * AuthResponse response = h.exchange(code);
     * System.out.println("OAuth token: " + response.accessToken());
     * }</pre>
     */
    @Contract("_ -> new")
    public static @NotNull AuthHandlerBuilder auth(@NotNull AuthScheme scheme) {
        return switch (scheme) {
            case SCREEN_CODE -> new ScreenCodeAuthHandlerBuilder();
            case CODE -> new CodeAuthHandlerBuilder();
            case LOCAL_CODE -> new LocalCodeAuthHandlerBuilder();
        };
    }

    /**
     * Creates a new {@link YanDisk} instance, using the provided function as an access token source.
     * Access tokens can be generated via {@link #auth(AuthScheme)}.
     */
    public static @NotNull YanDisk yanDisk(@NotNull Supplier<String> accessTokenSupplier) {
        return new YanDisk(new YanDiskImpl(accessTokenSupplier));
    }

    /**
     * Creates a new {@link YanDisk} instance from the specified constant access token.
     * Access tokens can be generated via {@link #auth(AuthScheme)}.
     */
    public static @NotNull YanDisk yanDisk(final @NotNull String accessToken) {
        return yanDisk(() -> accessToken);
    }

    /**
     * Creates a new {@link YanDisk} instance from the specified constant access token.
     * Access tokens can be generated via {@link #auth(AuthScheme)}.
     */
    public static @NotNull YanDisk yanDisk(@NotNull AuthResponse authResponse) {
        return yanDisk(authResponse.accessToken());
    }

    //

    private final YanDiskImpl impl;
    YanDisk(@NotNull YanDiskImpl impl) {
        this.impl = impl;
    }

    @Override
    public @NotNull List<Node> listAll(int limit, int offset) throws YanDiskException {
        return this.impl.listAll(limit, offset);
    }

    @Override
    public @NotNull List<Node> list(@NotNull NodePath root, int limit, int offset) throws YanDiskException {
        return this.impl.list(root, limit, offset);
    }

    @Override
    public @NotNull NodeUploader upload(@NotNull NodePath path, boolean overwrite) throws YanDiskException {
        return this.impl.upload(path, overwrite);
    }

    @Override
    public @NotNull NodeDownloader download(@NotNull NodePath path) throws YanDiskException {
        return this.impl.download(path);
    }

    @Override
    public @NotNull Operation copy(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException {
        return this.impl.copy(a, b, overwrite);
    }

    @Override
    public @NotNull Operation move(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException {
        return this.impl.move(a, b, overwrite);
    }

    @Override
    public @NotNull Operation delete(@NotNull NodePath path, boolean permanent) throws YanDiskException {
        return this.impl.delete(path, permanent);
    }

    @Override
    public boolean mkdir(@NotNull NodePath path, boolean lazy) throws YanDiskException {
        return this.impl.mkdir(path, lazy);
    }

}
