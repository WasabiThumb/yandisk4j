package io.github.wasabithumb.yandisk4j.node;

import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public record FileNode(
        @NotNull NodePath path,
        @NotNull String name,
        @NotNull OffsetDateTime created,
        @NotNull OffsetDateTime modified,
        @NotNull String md5,
        @NotNull String mimeType,
        long size
) implements Node {

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

}
