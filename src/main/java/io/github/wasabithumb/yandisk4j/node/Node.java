package io.github.wasabithumb.yandisk4j.node;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import io.github.wasabithumb.yandisk4j.util.JsonUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

@ApiStatus.NonExtendable
public sealed interface Node permits FileNode, DirectoryNode {

    static @NotNull Node fromJson(@NotNull JsonObject json) throws IllegalArgumentException {
        final String type = JsonUtil.getStringProperty(json, "type");
        final String name = JsonUtil.getStringProperty(json, "name");
        final NodePath path = NodePath.parse(JsonUtil.getStringProperty(json, "path"));
        final OffsetDateTime created = OffsetDateTime.parse(JsonUtil.getStringProperty(json, "created"));
        final OffsetDateTime modified = OffsetDateTime.parse(JsonUtil.getStringProperty(json, "modified"));

        switch (type) {
            case "file":
                final String md5 = JsonUtil.getStringProperty(json, "md5");
                final String mimeType = JsonUtil.getStringProperty(json, "mime_type");
                final long size = JsonUtil.getLongProperty(json, "size");
                return new FileNode(path, name, created, modified, md5, mimeType, size);
            case "dir":
                return new DirectoryNode(path, name, created, modified);
            default:
                throw new IllegalArgumentException("Invalid node type \"" + type + "\"");
        }
    }

    //

    @NotNull NodePath path();

    @NotNull String name();

    boolean isFile();

    boolean isDirectory();

    @NotNull OffsetDateTime created();

    @NotNull OffsetDateTime modified();

}
