package io.github.wasabithumb.yandisk4j.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@ApiStatus.Internal
public final class JsonUtil {

    public static @NotNull String getStringProperty(@NotNull JsonObject object, @NotNull String key) throws IllegalArgumentException {
        JsonElement element = object.get(key);
        JsonPrimitive primitive;
        if (element == null || !element.isJsonPrimitive() || !(primitive = element.getAsJsonPrimitive()).isString())
            throw new IllegalArgumentException("JSON object has no string property \"" + key + "\"");
        return primitive.getAsString();
    }

    public static @Nullable String getOptionalStringProperty(@NotNull JsonObject object, @NotNull String key) {
        JsonElement element = object.get(key);
        JsonPrimitive primitive;
        if (element != null && element.isJsonPrimitive() && (primitive = element.getAsJsonPrimitive()).isString())
            return primitive.getAsString();
        return null;
    }

    public static long getLongProperty(@NotNull JsonObject object, @NotNull String key) throws IllegalArgumentException {
        JsonElement element = object.get(key);
        JsonPrimitive primitive;
        if (element == null || !element.isJsonPrimitive() || !(primitive = element.getAsJsonPrimitive()).isNumber())
            throw new IllegalArgumentException("JSON object has no number property \"" + key + "\"");
        return primitive.getAsLong();
    }

    public static @NotNull JsonObject getObjectProperty(@NotNull JsonObject object, @NotNull String key) throws IllegalArgumentException {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonObject())
            throw new IllegalArgumentException("JSON object has no object property \"" + key + "\"");
        return element.getAsJsonObject();
    }

    public static @NotNull List<JsonObject> getObjectListProperty(@NotNull JsonObject object, @NotNull String key) throws IllegalArgumentException {
        return getObjectListProperty(object, key, (JsonObject ob) -> ob);
    }

    public static <T> @NotNull List<T> getObjectListProperty(
            @NotNull JsonObject object,
            @NotNull String key,
            @NotNull Function<JsonObject, T> mapper
    ) throws IllegalArgumentException {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonArray())
            throw new IllegalArgumentException("JSON object has no array property \"" + key + "\"");

        JsonArray array = element.getAsJsonArray();
        final List<T> ret = new ArrayList<>();

        JsonElement sub;
        for (int i=0; i < array.size(); i++) {
            sub = array.get(i);
            if (sub == null || !sub.isJsonObject())
                throw new IllegalArgumentException("JSON object has non-object member @ " + key + "[" + i + "]");
            ret.add(mapper.apply(sub.getAsJsonObject()));
        }

        return Collections.unmodifiableList(ret);
    }

}
