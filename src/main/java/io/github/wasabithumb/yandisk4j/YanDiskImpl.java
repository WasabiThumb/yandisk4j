package io.github.wasabithumb.yandisk4j;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.*;
import io.github.wasabithumb.yandisk4j.node.Node;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeDownloader;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeUploader;
import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import io.github.wasabithumb.yandisk4j.operation.Operation;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import io.github.wasabithumb.yandisk4j.util.YanDiskConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
final class YanDiskImpl implements IYanDisk {

    private final Gson gson;
    private final Supplier<String> accessTokenSupplier;
    YanDiskImpl(@NotNull Supplier<String> accessTokenSupplier) {
        this.gson = new Gson();
        this.accessTokenSupplier = accessTokenSupplier;
    }

    //

    private @NotNull String accessToken() {
        return Objects.requireNonNull(this.accessTokenSupplier.get());
    }

    private @NotNull HttpURLConnection open(@NotNull String endpoint, @NotNull String method) throws IOException {
        final URL url = URI.create("https://cloud-api.yandex.net/v1/disk/resources" + endpoint).toURL();
        final HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("User-Agent", YanDiskConstants.USER_AGENT);
        c.setRequestProperty("Authorization", "OAuth " + this.accessToken());
        return c;
    }

    private @NotNull HttpURLConnection get(@NotNull String endpoint) throws IOException {
        return this.open(endpoint, "GET");
    }

    private @NotNull HttpURLConnection post(@NotNull String endpoint) throws IOException {
        return this.open(endpoint, "POST");
    }

    private @NotNull JsonObject readJSON(@NotNull HttpURLConnection c) throws IOException, YanDiskException {
        InputStream is;
        int responseCode = c.getResponseCode();
        boolean isError;

        if (responseCode >= 500) {
            throw new YanDiskGatewayException(responseCode);
        } else if (responseCode >= 400) {
            is = c.getErrorStream();
            if (is == null) throw newUnknownClientError(responseCode);
            isError = true;
        } else {
            is = c.getInputStream();
            isError = false;
        }

        try (is; InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject ret = this.gson.fromJson(r, JsonObject.class);
            if (ret.has("error")) throw YanDiskAPIException.fromJSON(ret);
            if (isError) throw newUnknownClientError(responseCode);
            return ret;
        }
    }

    private YanDiskIOException newUnknownClientError(int code) {
        return new YanDiskIOException(
                "API reported a client error but did not provide further information",
                new IOException("Non-2XX response code " + code)
        );
    }

    //

    @Override
    public @NotNull List<Node> listAll(int limit, int offset) throws YanDiskException {
        JsonObject json;
        try {
            json = this.readJSON(this.get("/files?limit=" + limit + "&offset=" + offset));
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to fetch file tree", e);
        }
        return JsonUtil.getObjectListProperty(json, "items", Node::fromJson);
    }

    @Override
    public @NotNull List<Node> list(@NotNull NodePath root, int limit, int offset) throws YanDiskException {
        JsonObject json;
        try {
            json = this.readJSON(this.get(
                    "?path=" + URLEncoder.encode(root.toString(), StandardCharsets.UTF_8) +
                            "&limit=" + limit + "&offset=" + offset + "&sort=path"
            ));
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to fetch directory listing @ " + root, e);
        }

        if (!json.get("type").getAsString().equals("dir")) {
            throw new YanDiskOperationException("Path \"" + root + "\" is not a directory");
        }

        return JsonUtil.getObjectListProperty(
                JsonUtil.getObjectProperty(json, "_embedded"),
                "items",
                Node::fromJson
        );
    }

    @Override
    public @NotNull NodeUploader upload(@NotNull NodePath path, boolean overwrite) throws YanDiskException {
        JsonObject json;
        try {
            json = this.readJSON(this.get(
                    "/upload?path=" + URLEncoder.encode(path.toString(), StandardCharsets.UTF_8) +
                            "&overwrite=" + overwrite
            ));
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to upload file @ " + path, e);
        }
        return NodeUploader.fromJson(json);
    }

    @Override
    public @NotNull NodeDownloader download(@NotNull NodePath path) throws YanDiskException {
        JsonObject json;
        try {
            json = this.readJSON(this.get(
                    "/download?path=" + URLEncoder.encode(path.toString(), StandardCharsets.UTF_8)
            ));
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to download file @ " + path, e);
        }
        return NodeDownloader.fromJson(json);
    }

    @Override
    public @NotNull Operation copy(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException {
        JsonObject json;
        int status;
        try {
            HttpURLConnection connection = this.post(
                    "/copy?from=" + URLEncoder.encode(a.toString(), StandardCharsets.UTF_8) +
                            "&path=" + URLEncoder.encode(b.toString(), StandardCharsets.UTF_8) +
                            "&overwrite=" + overwrite
            );
            status = connection.getResponseCode();
            json = this.readJSON(connection);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to copy file from " + a + " to " + b, e);
        }
        if (status == 202) {
            return Operation.pending(json);
        }
        return Operation.SUCCESS;
    }

    @Override
    public @NotNull Operation move(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException {
        JsonObject json;
        int status;
        try {
            HttpURLConnection connection = this.post(
                    "/move?from=" + URLEncoder.encode(a.toString(), StandardCharsets.UTF_8) +
                            "&path=" + URLEncoder.encode(b.toString(), StandardCharsets.UTF_8) +
                            "&overwrite=" + overwrite
            );
            status = connection.getResponseCode();
            json = this.readJSON(connection);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to move file from " + a + " to " + b, e);
        }
        if (status == 202) {
            return Operation.pending(json);
        }
        return Operation.SUCCESS;
    }

    @Override
    public @NotNull Operation delete(@NotNull NodePath path, boolean permanent) throws YanDiskException {
        JsonObject json;
        int status;
        try {
            HttpURLConnection connection = this.open(
                    "?path=" + URLEncoder.encode(path.toString(), StandardCharsets.UTF_8) +
                            "&permanently=" + permanent,
                    "DELETE"
            );
            status = connection.getResponseCode();
            json = this.readJSON(connection);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to delete file @ " + path, e);
        }
        if (status == 202) {
            return Operation.pending(json);
        }
        return Operation.SUCCESS;
    }

    @Override
    public boolean mkdir(@NotNull NodePath path, boolean lazy) throws YanDiskException {
        try {
            HttpURLConnection connection = this.open(
                    "?path=" + URLEncoder.encode(path.toString(), StandardCharsets.UTF_8),
                    "PUT"
            );
            this.readJSON(connection);
        } catch (IOException e) {
            throw new YanDiskIOException("Failed to create directory @ " + path, e);
        } catch (YanDiskAPIException e) {
            if (lazy && "DiskPathPointsToExistentDirectoryError".equals(e.errorCode())) return false;
            throw e;
        }
        return true;
    }

}
