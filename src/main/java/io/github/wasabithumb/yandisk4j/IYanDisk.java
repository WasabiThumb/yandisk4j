package io.github.wasabithumb.yandisk4j;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.node.Node;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeDownloader;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeUploader;
import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import io.github.wasabithumb.yandisk4j.operation.Operation;
import io.github.wasabithumb.yandisk4j.util.PaginatedResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;

public sealed interface IYanDisk permits YanDiskImpl, YanDisk {

    // https://yandex.com/dev/disk-api/doc/en/reference/all-files

    /**
     * List all files (<a href="https://yandex.com/dev/disk-api/doc/en/reference/all-files">see reference</a>).
     * This is <strong>paginated</strong>.
     * To easily traverse between pages, use {@link #listAll()}/{@link #listAll(int)}.
     * @param limit The maximum number of entries that can be returned by this method.
     * @param offset The first entry index to include.
     */
    @NotNull List<Node> listAll(int limit, int offset) throws YanDiskException;

    /**
     * List all files (<a href="https://yandex.com/dev/disk-api/doc/en/reference/all-files">see reference</a>).
     * The {@link PaginatedResult#iterator()} method can be used to join together all pages of the result, but
     * be aware that this can sneakily throw {@link YanDiskException}.
     * @param pageSize The number of files on each page.
     * @see #listAll(int, int)
     */
    default @NotNull PaginatedResult<Node> listAll(int pageSize) throws YanDiskException {
        return new PaginatedResult<>(this::listAll, pageSize);
    }

    /**
     * List all files (<a href="https://yandex.com/dev/disk-api/doc/en/reference/all-files">see reference</a>).
     * Alias for {@code listAll(20)}.
     * @see #listAll(int)
     */
    default @NotNull PaginatedResult<Node> listAll() throws YanDiskException {
        return this.listAll(20);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/meta

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * This is <strong>paginated</strong>.
     * To easily traverse between pages, use {@link #list(NodePath)}/{@link #list(NodePath, int)}.
     * @param root The path to list the content of
     * @param limit The maximum number of entries that can be returned by this method.
     * @param offset The first entry index to include.
     */
    @NotNull List<Node> list(@NotNull NodePath root, int limit, int offset) throws YanDiskException;

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * The {@link PaginatedResult#iterator()} method can be used to join together all pages of the result, but
     * be aware that this can sneakily throw {@link YanDiskException}.
     * @param root The path to list the content of
     * @param pageSize The number of files on each page.
     * @see #list(NodePath, int, int)
     */
    default @NotNull PaginatedResult<Node> list(@NotNull NodePath root, int pageSize) throws YanDiskException {
        return new PaginatedResult<>((int limit, int offset) -> this.list(root, limit, offset), pageSize);
    }

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * Alias for {@code list(root, 20)}.
     * @param root The path to list the content of
     * @see #list(NodePath, int)
     */
    default @NotNull PaginatedResult<Node> list(@NotNull NodePath root) throws YanDiskException {
        return this.list(root, 20);
    }

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * This is <strong>paginated</strong>.
     * To easily traverse between pages, use {@link #list(String)}/{@link #list(String, int)}.
     * @param root The path to list the content of
     * @param limit The maximum number of entries that can be returned by this method.
     * @param offset The first entry index to include.
     * @see #list(NodePath, int, int)
     */
    default @NotNull List<Node> list(@NotNull String root, int limit, int offset) throws YanDiskException {
        return this.list(NodePath.parse(root), limit, offset);
    }

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * The {@link PaginatedResult#iterator()} method can be used to join together all pages of the result, but
     * be aware that this can sneakily throw {@link YanDiskException}.
     * @param root The path to list the content of
     * @param pageSize The number of files on each page.
     * @see #list(String, int, int)
     */
    default @NotNull PaginatedResult<Node> list(@NotNull String root, int pageSize) throws YanDiskException {
        return this.list(NodePath.parse(root), pageSize);
    }

    /**
     * List all files parented to the given root path (<a href="https://yandex.com/dev/disk-api/doc/en/reference/meta">see reference</a>).
     * Alias for {@code list(root, 20)}.
     * @param root The path to list the content of
     * @see #list(String, int)
     */
    default @NotNull PaginatedResult<Node> list(@NotNull String root) throws YanDiskException {
        return this.list(NodePath.parse(root), 20);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/upload

    /**
     * Provisions a file upload. Use {@link NodeUploader#write(InputStream)} to complete the upload.
     * @param path The path to write to
     * @param overwrite If true, any existing file will be overwritten.
     */
    @NotNull NodeUploader upload(@NotNull NodePath path, boolean overwrite) throws YanDiskException;

    /**
     * Provisions a file upload. Use {@link NodeUploader#write(InputStream)} to complete the upload.
     * Alias for {@code upload(path, false)}.
     * @param path The path to write to
     * @see #upload(NodePath, boolean)
     */
    default @NotNull NodeUploader upload(@NotNull NodePath path) throws YanDiskException {
        return this.upload(path, false);
    }

    /**
     * Provisions a file upload. Use {@link NodeUploader#write(InputStream)} to complete the upload.
     * @param path The path to write to
     * @param overwrite If true, any existing file will be overwritten.
     * @see #upload(NodePath, boolean)
     */
    default @NotNull NodeUploader upload(@NotNull String path, boolean overwrite) throws YanDiskException {
        return this.upload(NodePath.parse(path), overwrite);
    }

    /**
     * Provisions a file upload. Use {@link NodeUploader#write(InputStream)} to complete the upload.
     * Alias for {@code upload(path, false)}.
     * @param path The path to write to
     * @see #upload(String, boolean)
     */
    default @NotNull NodeUploader upload(@NotNull String path) throws YanDiskException {
        return this.upload(NodePath.parse(path), false);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/content

    /**
     * Provisions a file download. Use {@link NodeDownloader#read()} to complete the download.
     * @param path The path to read from
     */
    @NotNull NodeDownloader download(@NotNull NodePath path) throws YanDiskException;

    /**
     * Provisions a file download. Use {@link NodeDownloader#read()} to complete the download.
     * @param path The path to read from
     */
    default @NotNull NodeDownloader download(@NotNull String path) throws YanDiskException {
        return this.download(NodePath.parse(path));
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/copy

    /**
     * Copies a file or folder.
     * @param a Path of the node to copy.
     * @param b Path of the destination node.
     * @param overwrite If true, permits overwriting.
     */
    @NotNull Operation copy(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException;

    /**
     * Copies a file or folder. Alias for {@code copy(a, b, false)}
     * @param a Path of the node to copy.
     * @param b Path of the destination node.
     * @see #copy(NodePath, NodePath, boolean)
     */
    default @NotNull Operation copy(@NotNull NodePath a, @NotNull NodePath b) throws YanDiskException {
        return this.copy(a, b, false);
    }

    /**
     * Copies a file or folder.
     * @param a Path of the node to copy.
     * @param b Path of the destination node.
     * @param overwrite If true, permits overwriting.
     * @see #copy(NodePath, NodePath, boolean)
     */
    default @NotNull Operation copy(@NotNull String a, @NotNull String b, boolean overwrite) throws YanDiskException {
        return this.copy(NodePath.parse(a), NodePath.parse(b), overwrite);
    }

    /**
     * Copies a file or folder. Alias for {@code copy(a, b, false)}
     * @param a Path of the node to copy.
     * @param b Path of the destination node.
     * @see #copy(String, String, boolean)
     */
    default @NotNull Operation copy(@NotNull String a, @NotNull String b) throws YanDiskException {
        return this.copy(NodePath.parse(a), NodePath.parse(b), false);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/move

    /**
     * Moves a file or folder.
     * @param a Path of the node to move.
     * @param b Path of the destination node.
     * @param overwrite If true, permits overwriting.
     */
    @NotNull Operation move(@NotNull NodePath a, @NotNull NodePath b, boolean overwrite) throws YanDiskException;

    /**
     * Moves a file or folder. Alias for {@code move(a, b, false)}
     * @param a Path of the node to move.
     * @param b Path of the destination node.
     * @see #move(NodePath, NodePath, boolean)
     */
    default @NotNull Operation move(@NotNull NodePath a, @NotNull NodePath b) throws YanDiskException {
        return this.move(a, b, false);
    }

    /**
     * Moves a file or folder.
     * @param a Path of the node to move.
     * @param b Path of the destination node.
     * @param overwrite If true, permits overwriting.
     * @see #move(NodePath, NodePath, boolean)
     */
    default @NotNull Operation move(@NotNull String a, @NotNull String b, boolean overwrite) throws YanDiskException {
        return this.move(NodePath.parse(a), NodePath.parse(b), overwrite);
    }

    /**
     * moves a file or folder. Alias for {@code move(a, b, false)}
     * @param a Path of the node to move.
     * @param b Path of the destination node.
     * @see #move(String, String, boolean)
     */
    default @NotNull Operation move(@NotNull String a, @NotNull String b) throws YanDiskException {
        return this.move(NodePath.parse(a), NodePath.parse(b), false);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/delete

    /**
     * Deletes a file or folder.
     * @param path Path of the node to delete.
     * @param permanent If true, node is deleted permanently. Otherwise, node is moved to Trash.
     */
    @NotNull Operation delete(@NotNull NodePath path, boolean permanent) throws YanDiskException;

    /**
     * Deletes a file or folder. Alias for {@code delete(path, false)}.
     * @param path Path of the node to delete.
     * @see #delete(NodePath, boolean)
     */
    default @NotNull Operation delete(@NotNull NodePath path) throws YanDiskException {
        return this.delete(path, false);
    }

    /**
     * Deletes a file or folder.
     * @param path Path of the node to delete.
     * @param permanent If true, node is deleted permanently. Otherwise, node is moved to Trash.
     * @see #delete(NodePath, boolean)
     */
    default @NotNull Operation delete(@NotNull String path, boolean permanent) throws YanDiskException {
        return this.delete(NodePath.parse(path), permanent);
    }

    /**
     * Deletes a file or folder. Alias for {@code delete(path, false)}.
     * @param path Path of the node to delete.
     * @see #delete(String, boolean)
     */
    default @NotNull Operation delete(@NotNull String path) throws YanDiskException {
        return this.delete(NodePath.parse(path), false);
    }

    // https://yandex.com/dev/disk-api/doc/en/reference/create-folder

    /**
     * Creates a folder.
     * @param path Path to the folder to create.
     * @param lazy If true, this method will not throw when the directory already exists.
     * @return True if the directory was created
     * @since 0.4.0
     */
    @Contract("_, false -> true")
    boolean mkdir(@NotNull NodePath path, boolean lazy) throws YanDiskException;

    /**
     * Creates a folder. Alias for {@code mkdir(path, false)}.
     * @param path Path to the folder to create.
     * @see #mkdir(NodePath, boolean)
     */
    default void mkdir(@NotNull NodePath path) throws YanDiskException {
        this.mkdir(path, false);
    }

    /**
     * Creates a folder.
     * @param path Path to the folder to create.
     * @param lazy If true, this method will not throw when the directory already exists.
     * @return True if the directory was created
     * @since 0.4.0
     */
    @Contract("_, false -> true")
    default boolean mkdir(@NotNull String path, boolean lazy) throws YanDiskException {
        return this.mkdir(NodePath.parse(path), lazy);
    }

    /**
     * Creates a folder. Alias for {@code mkdir(path, false)}.
     * @param path Path to the folder to create.
     * @see #mkdir(String, boolean)
     */
    default void mkdir(@NotNull String path) throws YanDiskException {
        this.mkdir(NodePath.parse(path));
    }

}
