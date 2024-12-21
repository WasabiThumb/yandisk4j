package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskAPIException;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.except.YanDiskOperationException;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@ApiStatus.Internal
public final class NodeAwaiter extends AbstractNodeAccessor {

    private static final Gson GSON = new Gson();
    public static @NotNull NodeAwaiter fromJson(@NotNull JsonObject json) {
        return AbstractNodeAccessor.fromJson(json, NodeAwaiter::new);
    }

    public NodeAwaiter(@NotNull String href, @NotNull String method) {
        super(href, method);
    }

    public @NotNull String getStatus() throws YanDiskException {
        JsonObject ob;
        try {
            HttpURLConnection c = this.openConnection();
            c.setRequestProperty("Accept", "application/json");

            try (InputStream is = c.getInputStream();
                 InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)
            ) {
                ob = GSON.fromJson(r, JsonObject.class);
            }
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to get status", e);
        }

        if (ob.has("error"))
            throw YanDiskAPIException.fromJSON(ob);

        try {
            return JsonUtil.getStringProperty(ob, "status");
        } catch (IllegalArgumentException e) {
            throw new YanDiskOperationException("Status endpoint provided invalid data");
        }
    }

}
