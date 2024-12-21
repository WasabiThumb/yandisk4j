package io.github.wasabithumb.yandisk4j.node.path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class ConcatNodePath implements NodePath {

    private final List<CharSequence> parts;
    private final CharSequence protocol;
    private int length = -1;
    ConcatNodePath(@NotNull List<CharSequence> parts, @Nullable CharSequence protocol) {
        this.parts = Collections.unmodifiableList(parts);
        this.protocol = protocol;
    }

    @Override
    public @Nullable CharSequence protocol() {
        return this.protocol;
    }

    @Override
    public @NotNull List<CharSequence> parts() {
        return this.parts;
    }

    @Override
    public synchronized int length() {
        if (this.length != -1) return this.length;
        int sum = 1;
        if (this.protocol != null) {
            sum += this.protocol.length() + 1;
        }
        for (int i=0; i < this.parts.size(); i++) {
            if (i != 0) sum++;
            sum += this.parts.get(i).length();
        }
        return this.length = sum;
    }

    @Override
    public char charAt(int i) {
        final int query = i;
        if (i < 0)
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds (must be positive)");

        if (this.protocol != null) {
            final int len = this.protocol.length();
            if (i < len) {
                return this.protocol.charAt(i);
            } else if (i == len) {
                return ':';
            }
            i -= (len + 1);
        }
        if (i == 0) {
            return '/';
        }
        i--;

        Iterator<CharSequence> iter = this.parts.iterator();
        CharSequence part;
        while (iter.hasNext()) {
            part = iter.next();
            final int len = part.length();
            if (i < len) return part.charAt(i);
            i -= len;
            if (i == 0 && iter.hasNext()) return '/';
            i--;
        }

        throw new IndexOutOfBoundsException("Index " + query + " out of bounds for length " + this.length());
    }

    @Override
    public @NotNull CharSequence subSequence(int i, int i1) {
        return this.toString().subSequence(i, i1);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder(this.length());

        if (this.protocol != null) {
            sb.append(this.protocol);
            sb.append(':');
        }
        sb.append('/');

        for (int i=0; i < this.parts.size(); i++) {
            if (i != 0) sb.append('/');
            sb.append(this.parts.get(i));
        }
        return sb.toString();
    }

}
