package io.github.wasabithumb.yandisk4j.node.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A path for Yandex Disk, in the format {@code [protocol:]/a/b/c}.
 * For conversion, you likely want to use {@link #parse(CharSequence)}. The
 * {@link #of(CharSequence, CharSequence...)} method and variants are also provide to create paths without
 * any parsing (can be used to create invalid paths).
 */
@ApiStatus.NonExtendable
public sealed interface NodePath extends CharSequence permits LiteralNodePath, ConcatNodePath {

    @ApiStatus.Experimental
    static @NotNull NodePath of(@Nullable CharSequence protocol, @NotNull CharSequence @NotNull ... parts) {
        return new ConcatNodePath(Arrays.asList(parts), protocol);
    }

    @ApiStatus.Experimental
    static @NotNull NodePath of(@Nullable CharSequence protocol, @NotNull List<String> parts) {
        return new ConcatNodePath(
                parts.stream().map(CharBuffer::wrap).collect(Collectors.toUnmodifiableList()),
                protocol
        );
    }

    @ApiStatus.Experimental
    static @NotNull NodePath of(@NotNull CharSequence @NotNull ... parts) {
        return new ConcatNodePath(Arrays.asList(parts), null);
    }

    @ApiStatus.Experimental
    static @NotNull NodePath of(@NotNull List<String> parts) {
        return new ConcatNodePath(
                parts.stream().map(CharBuffer::wrap).collect(Collectors.toUnmodifiableList()),
                null
        );
    }

    /**
     * Parses a disk path from a given notation string (e.g. {@code [protocol:]/a/b/c})
     */
    static @NotNull NodePath parse(@NotNull CharSequence notation) throws IllegalArgumentException {
        if (notation.isEmpty())
            return of(Collections.emptyList());

        CharSequence protocol = null;
        CharSequence path;
        char c;

        if (notation.charAt(0) == '/') {
            path = notation;
        } else {
            int whereProtocol = -1;
            for (int i=0; i < notation.length(); i++) {
                c = notation.charAt(i);
                if (c == ':') {
                    if (i == 0)
                        throw new IllegalArgumentException("Protocol section of path \"" + notation + "\" is empty");
                    if (i == (notation.length() - 1))
                        throw new IllegalArgumentException("Path \"" + notation + "\" contains only a protocol");
                    if (notation.charAt(i + 1) != '/')
                        throw new IllegalArgumentException("Protocol symbol in path \"" + notation + "\" must be followed by slash");
                    whereProtocol = i;
                    break;
                }
                if (c == '/') break;
            }
            if (whereProtocol != -1) {
                protocol = notation.subSequence(0, whereProtocol);
                path = notation.subSequence(whereProtocol + 1, notation.length());
            } else {
                path = notation;
            }
        }

        boolean literal = path.charAt(0) == '/';
        if (path.length() < 2) {
            return literal ?
                    new LiteralNodePath(notation, protocol) :
                    new ConcatNodePath(Collections.singletonList(path), protocol);
        }
        path = path.subSequence(1, path.length());
        if (path.charAt(path.length() - 1) == '/') literal = false;

        LiteralNodePath value = new LiteralNodePath(notation, protocol);
        if (literal) return value;

        return new ConcatNodePath(
                value.parts(),
                protocol
        );
    }

    /**
     * Parses a disk path from a given notation string (e.g. {@code [protocol:]/a/b/c})
     */
    static @NotNull NodePath parse(@NotNull String notation) throws IllegalArgumentException {
        return parse(CharBuffer.wrap(notation));
    }

    /**
     * Joins 2 paths together.
     * @throws IllegalArgumentException Joining the paths would cause unwanted protocol coercion (the 2nd path
     * has a protocol set, and this protocol differs from the protocol of the 1st path)
     */
    @Contract("_, _ -> new")
    static @NotNull NodePath join(@NotNull NodePath a, @NotNull NodePath b) throws IllegalArgumentException {
        CharSequence bm = b.protocol();
        if (bm != null) {
            CharSequence am = a.protocol();
            if (am == null || !bm.toString().contentEquals(am))
                throw new IllegalArgumentException("Cannot append \"" + a + "\" to \"" + b + "\" (protocol coercion)");
        }

        List<CharSequence> ap = a.parts();
        int apl = ap.size();
        List<CharSequence> bp = b.parts();
        int bpl = bp.size();

        CharSequence[] both = new CharSequence[apl + bpl];
        for (int i=0; i < apl; i++) both[i]       = ap.get(i);
        for (int i=0; i < bpl; i++) both[apl + i] = bp.get(i);
        return new ConcatNodePath(
                Arrays.asList(both),
                a.protocol()
        );
    }

    //

    /**
     * The protocol segment of the path, if any.
     */
    @Nullable CharSequence protocol();

    /**
     * The "parts" of the path. For example, if the path is defined as {@code /a/b/c}, the parts are
     * {@code ["a", "b", "c"]}.
     */
    @NotNull List<CharSequence> parts();

}
