package io.github.wasabithumb.yandisk4j.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskAPIException;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.util.YanDiskConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Iterator;

@ApiStatus.Internal
public abstract class AbstractAuthHandler implements AuthHandler {

    private static final Gson GSON = new Gson();
    private static final String EXCHANGE_URL = "https://oauth.yandex.com/token?grant_type=authorization_code";

    protected final String clientID;
    protected final String clientSecret;
    protected final EnumSet<AuthScope> scopes;
    protected final String deviceID;
    protected final String deviceName;
    protected final String state;
    public AbstractAuthHandler(
            @NotNull String clientID,
            @NotNull String clientSecret,
            @NotNull EnumSet<AuthScope> scopes,
            @Nullable String deviceID,
            @Nullable String deviceName,
            @Nullable String state
    ) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        this.state = state;
    }

    @Override
    public abstract @NotNull AuthScheme scheme();

    protected abstract @Nullable String getRedirectURI();

    @Override
    public @NotNull String getURL() {
        StringBuilder sb = new StringBuilder("https://oauth.yandex.com/authorize?response_type=code");
        sb.append("&client_id=").append(URLEncoder.encode(this.clientID, StandardCharsets.UTF_8));

        this.appendDeviceInfo(sb);

        String redirectURI = this.getRedirectURI();
        if (redirectURI != null)
            sb.append("&redirect_uri=").append(URLEncoder.encode(redirectURI, StandardCharsets.UTF_8));

        if (this.state != null)
            sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));

        Iterator<AuthScope> scopes = this.scopes.iterator();
        if (scopes.hasNext()) {
            sb.append("&scope=")
                    .append(URLEncoder.encode(scopes.next().token(), StandardCharsets.UTF_8));

            while (scopes.hasNext())
                sb.append("%20").append(URLEncoder.encode(scopes.next().token(), StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    @Override
    public void openURL() {
        final String url = this.getURL();
        Exception suppressed = null;

        if (Desktop.isDesktopSupported()) {
            final Desktop d = Desktop.getDesktop();
            if (d.isSupported(Desktop.Action.BROWSE)) {
                try {
                    d.browse(URI.create(url));
                    return;
                } catch (IOException e) {
                    suppressed = e;
                }
            }
        }

        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("xdg-open " + url);
        } catch (IOException e) {
            if (suppressed != null) e.addSuppressed(suppressed);
            throw new AssertionError("Failed to open browser", e);
        }
    }

    protected @NotNull String getExchangeBody(@NotNull String code) {
        StringBuilder sb = new StringBuilder("grant_type=authorization_code");
        sb.append("&code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        this.appendDeviceInfo(sb);
        return sb.toString();
    }

    @Override
    public @NotNull AuthResponse exchange(@NotNull String code) throws YanDiskException {
        try {
            final URL url = URI.create(EXCHANGE_URL).toURL();
            final byte[] body = this.getExchangeBody(code).getBytes(StandardCharsets.UTF_8);
            final HttpURLConnection c = (HttpURLConnection) url.openConnection();

            String auth = this.clientID + ":" + this.clientSecret;
            auth = new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

            c.setRequestMethod("POST");
            c.setDoInput(true);
            c.setDoOutput(true);
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setRequestProperty("Content-Length", Integer.toString(body.length));
            c.setRequestProperty("Authorization", "Basic " + auth);
            c.setRequestProperty("User-Agent", YanDiskConstants.USER_AGENT);

            try (OutputStream os = c.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            final int status = c.getResponseCode();
            if (status < 200 || status > 299)
                throw new IOException("Non-2XX HTTP response code " + status + " (" + c.getResponseMessage() + ")");

            JsonObject object;
            try (InputStream is = c.getInputStream();
                 InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)
            ) {
                object = GSON.fromJson(r, JsonObject.class);
            }

            if (object.has("error")) {
                throw YanDiskAPIException.fromJSON(object);
            }
            return AuthResponse.fromJSON(object);
        } catch (IOException e) {
            throw new YanDiskIOException(e);
        }
    }

    protected void appendDeviceInfo(@NotNull StringBuilder sb) {
        if (this.deviceID != null)
            sb.append("&device_id=").append(URLEncoder.encode(this.deviceID, StandardCharsets.UTF_8));

        if (this.deviceName != null)
            sb.append("&device_name=").append(URLEncoder.encode(this.deviceName, StandardCharsets.UTF_8));
    }

}
