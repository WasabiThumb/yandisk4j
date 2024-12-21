package io.github.wasabithumb.yandisk4j.node.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
final class LiteralNodePath implements NodePath {

    private final CharSequence path;
    private final CharSequence protocol;
    private List<CharSequence> parts = null;
    LiteralNodePath(@NotNull CharSequence path, @Nullable CharSequence protocol) {
        this.path = path;
        this.protocol = protocol;
    }

    @Override
    public @Nullable CharSequence protocol() {
        return this.protocol;
    }

    @Override
    public synchronized @NotNull List<CharSequence> parts() {
        if (this.parts != null) return this.parts;
        List<CharSequence> ret = new LinkedList<>();

        CharSequence path = CharBuffer.wrap(this.path);
        if (this.protocol != null) {
            path = path.subSequence(this.protocol.length() + 1, path.length());
        }

        int start = 0;
        for (int i=0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                if (start != i)
                    ret.add(path.subSequence(start, i));
                start = i + 1;
            }
        }
        if (start != path.length())
            ret.add(path.subSequence(start, path.length()));

        return this.parts = Collections.unmodifiableList(ret);
    }

    @Override
    public int length() {
        return this.path.length();
    }

    @Override
    public char charAt(int i) {
        return this.path.charAt(i);
    }

    @Override
    public @NotNull CharSequence subSequence(int i, int i1) {
        return this.path.subSequence(i, i1);
    }

    @Override
    public @NotNull String toString() {
        return this.path.toString();
    }

}
