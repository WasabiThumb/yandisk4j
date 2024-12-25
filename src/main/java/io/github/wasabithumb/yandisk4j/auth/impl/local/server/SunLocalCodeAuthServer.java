package io.github.wasabithumb.yandisk4j.auth.impl.local.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.wasabithumb.yandisk4j.auth.AuthCodeResponse;
import io.github.wasabithumb.yandisk4j.except.YanDiskAPIException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@ApiStatus.Internal
class SunLocalCodeAuthServer implements LocalCodeAuthServer, HttpHandler {

    protected final InetSocketAddress socketAddress;
    protected final CompletableFuture<AuthCodeResponse> binding;
    protected HttpServer server;
    protected String bodyPrefix;
    protected String bodyPostfix;
    protected String bodyMessageSuccess;
    protected String bodyMessageError;

    public SunLocalCodeAuthServer(int port) {
        this.socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
        this.binding = new CompletableFuture<>();
    }

    @Override
    public void setResponseParams(
            @NotNull String bodyPrefix,
            @NotNull String bodyPostfix,
            @NotNull String bodyMessageSuccess,
            @NotNull String bodyMessageError
    ) {
        this.bodyPrefix = bodyPrefix;
        this.bodyPostfix = bodyPostfix;
        this.bodyMessageSuccess = bodyMessageSuccess;
        this.bodyMessageError = bodyMessageError;
    }

    @Override
    public void start() throws IOException {
        HttpServer server = HttpServer.create(this.socketAddress, 0);
        server.createContext("/", this);
        server.setExecutor(null);
        server.start();
        this.server = server;
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }

    @Override
    public @NotNull CompletableFuture<AuthCodeResponse> bind() {
        return this.binding;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (this.binding.isDone()) {
            this.respond(httpExchange, !this.binding.isCompletedExceptionally());
            return;
        }

        final Map<String, String> query = this.readQuery(httpExchange);
        String tmp;

        if ((tmp = query.get("code")) != null && !tmp.isEmpty()) {
            final String code = tmp;
            String state = null;
            if ((tmp = query.get("state")) != null && !tmp.isEmpty()) state = tmp;
            this.respond(httpExchange, true);
            this.binding.complete(new AuthCodeResponse(code, state));
            return;
        }

        String error = query.get("error");
        if (error == null) {
            this.respond(httpExchange, false);
            this.binding.completeExceptionally(
                    new AssertionError("Request does not contain one of: code, error")
            );
            return;
        }

        this.respond(httpExchange, false);
        this.binding.completeExceptionally(new YanDiskAPIException(error, query.get("error_description")));
    }

    private void respond(@NotNull HttpExchange exchange, boolean success) throws IOException {
        final String body = this.bodyPrefix + (success ? this.bodyMessageSuccess : this.bodyMessageError) + this.bodyPostfix;
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        final Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=UTF-8");
        headers.set("Server", "yandisk4j (sun); wasabithumbs@gmail.com");

        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private @NotNull Map<String, String> readQuery(@NotNull HttpExchange exchange) {
        String data = exchange.getRequestURI().getRawQuery();
        if (data == null) return Collections.emptyMap();

        Map<String, String> ret = new HashMap<>(4);
        int offset = 0;

        while (offset < data.length()) {
            int whereEq = data.indexOf('=', offset);
            boolean noEq = (whereEq == -1);
            int whereAmp = data.indexOf('&', offset);
            int end;
            if (whereAmp == -1) {
                end = data.length();
            } else if (whereAmp < whereEq) {
                noEq = true;
                end = whereAmp;
            } else {
                end = whereAmp;
            }

            if (noEq) {
                ret.put(
                        URLDecoder.decode(data.substring(offset, end), StandardCharsets.UTF_8),
                        ""
                );
            } else {
                ret.put(
                        URLDecoder.decode(data.substring(offset, whereEq), StandardCharsets.UTF_8),
                        URLDecoder.decode(data.substring(whereEq + 1, end), StandardCharsets.UTF_8)
                );
            }

            offset = end + 1;
        }

        return Collections.unmodifiableMap(ret);
    }

}
