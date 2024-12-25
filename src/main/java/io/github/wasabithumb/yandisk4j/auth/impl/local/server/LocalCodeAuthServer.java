package io.github.wasabithumb.yandisk4j.auth.impl.local.server;

import io.github.wasabithumb.yandisk4j.auth.AuthCodeResponse;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public interface LocalCodeAuthServer {

    static @NotNull LocalCodeAuthServer create(int port) {
        boolean httpd = false;
        try {
            Class.forName("fi.iki.elonen.NanoHTTPD");
            httpd = true;
        } catch (ClassNotFoundException ignored2) { }

        String clazz = "io.github.wasabithumb.yandisk4j.auth.impl.local.server." +
                (httpd ? "Nano" : "Sun") + "LocalCodeAuthServer";
        String type = httpd ? "NanoHTTPD" : "Sun";
        String warning = httpd ? "" : " (try installing NanoHTTPD)";

        try {
            Class<?> cls = Class.forName(clazz);
            Constructor<?> con = cls.getConstructor(Integer.TYPE);
            return (LocalCodeAuthServer) con.newInstance(port);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError("Failed to create " + type + " auth server" + warning, e);
        }
    }

    //

    void setResponseParams(
            @NotNull String bodyPrefix,
            @NotNull String bodyPostfix,
            @NotNull String bodyMessageSuccess,
            @NotNull String bodyMessageError
    );

    void start() throws IOException;

    void stop();

    @NotNull CompletableFuture<AuthCodeResponse> bind();

}
