package io.github.wasabithumb.yandisk4j.node;

import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public record DirectoryNode(
        @NotNull NodePath path,
        @NotNull String name,
        @NotNull OffsetDateTime created,
        @NotNull OffsetDateTime modified
) implements Node {

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
