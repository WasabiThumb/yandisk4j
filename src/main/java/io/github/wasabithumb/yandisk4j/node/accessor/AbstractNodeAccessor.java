package io.github.wasabithumb.yandisk4j.node.accessor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import io.github.wasabithumb.yandisk4j.util.YanDiskConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.BiFunction;

@ApiStatus.Internal
sealed class AbstractNodeAccessor implements NodeAccessor permits NodeAwaiter, NodeDownloader, NodeUploader {

    protected static <T extends AbstractNodeAccessor> @NotNull T fromJson(
            @NotNull JsonObject json,
            @NotNull BiFunction<String, String, T> construct
    ) throws IllegalArgumentException {
        final String href = JsonUtil.getStringProperty(json, "href");
        final String method = JsonUtil.getStringProperty(json, "method");
        JsonElement templated = json.get("templated");
        if (templated != null && templated.isJsonPrimitive()) {
            if (templated.getAsJsonPrimitive().getAsBoolean())
                throw new IllegalArgumentException("No rule to handle templated URL: " + href);
        }
        return construct.apply(href, method);
    }

    protected final String href;
    protected final String method;
    public AbstractNodeAccessor(@NotNull String href, @NotNull String method) {
        this.href = href;
        this.method = method;
    }

    @Override
    public @NotNull String href() {
        return this.href;
    }

    @Override
    public @NotNull String method() {
        return this.method;
    }

    @NotNull
    protected final HttpURLConnection openConnection() throws IOException {
        final URL url = URI.create(this.href()).toURL();
        final HttpURLConnection ret = (HttpURLConnection) url.openConnection();
        ret.setRequestMethod(this.method());
        ret.setRequestProperty("User-Agent", YanDiskConstants.USER_AGENT);
        return ret;
    }

}
