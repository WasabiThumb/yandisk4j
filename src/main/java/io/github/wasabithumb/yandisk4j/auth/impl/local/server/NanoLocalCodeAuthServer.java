package io.github.wasabithumb.yandisk4j.auth.impl.local.server;

import fi.iki.elonen.NanoHTTPD;
import io.github.wasabithumb.yandisk4j.auth.AuthCodeResponse;
import io.github.wasabithumb.yandisk4j.except.YanDiskAPIException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@ApiStatus.Internal
class NanoLocalCodeAuthServer extends NanoHTTPD implements LocalCodeAuthServer {

    protected final CompletableFuture<AuthCodeResponse> binding;
    protected String bodyPrefix;
    protected String bodyPostfix;
    protected String bodyMessageSuccess;
    protected String bodyMessageError;

    public NanoLocalCodeAuthServer(int port) {
        super(port);
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
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public @NotNull CompletableFuture<AuthCodeResponse> bind() {
        return this.binding;
    }

    @Override
    public synchronized Response serve(IHTTPSession session) {
        if (this.binding.isDone()) {
            return this.respond(!this.binding.isCompletedExceptionally());
        }

        AuthCodeResponse response;
        try {
            response = this.serve0(session);
        } catch (Throwable t) {
            this.binding.completeExceptionally(t);
            return this.respond(false);
        }

        this.binding.complete(response);
        return this.respond(true);
    }

    private @NotNull AuthCodeResponse serve0(@NotNull IHTTPSession session) {
        final Map<String, List<String>> params = session.getParameters();
        List<String> tmp;

        if ((tmp = params.get("code")) != null && tmp.size() == 1) {
            final String code = tmp.get(0);
            String state = null;
            if ((tmp = params.get("state")) != null && tmp.size() == 1)
                state = tmp.get(0);
            return new AuthCodeResponse(code, state);
        }

        String error;
        if ((tmp = params.get("error")) != null && tmp.size() == 1) {
            error = tmp.get(0);
        } else {
            throw new AssertionError("Request does not contain one of: code, error");
        }

        String desc = null;
        if ((tmp = params.get("error_description")) != null && tmp.size() == 1)
            desc = tmp.get(0);

        throw new YanDiskAPIException(error, desc);
    }

    private @NotNull Response respond(boolean success) {
        String body = this.bodyPrefix + (success ? this.bodyMessageSuccess : this.bodyMessageError) + this.bodyPostfix;
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        Response r = newFixedLengthResponse(
                Response.Status.OK,
                "text/html; charset=UTF-8",
                new ByteArrayInputStream(bytes),
                bytes.length
        );
        r.addHeader("Server", "yandisk4j (nanohttpd); wasabithumbs@gmail.com");
        return r;
    }

}
