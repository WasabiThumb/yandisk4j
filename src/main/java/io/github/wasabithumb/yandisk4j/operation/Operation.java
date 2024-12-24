package io.github.wasabithumb.yandisk4j.operation;

import com.google.gson.JsonObject;
import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeAwaiter;
import io.github.wasabithumb.yandisk4j.node.path.NodePath;
import io.github.wasabithumb.yandisk4j.util.Watchable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * <p>
 * An operation, which may be completed as-is, or may require requests to evaluate the {@link #status()}.
 * In the latter case, the status will only be updated after at least one call to {@link #status()} or
 * {@link #watch(Consumer)}.
 * </p><p>
 * The Operation class is currently only used for large
 * {@link io.github.wasabithumb.yandisk4j.IYanDisk#move(NodePath, NodePath, boolean) move},
 * {@link io.github.wasabithumb.yandisk4j.IYanDisk#copy(NodePath, NodePath, boolean) copy} and
 * {@link io.github.wasabithumb.yandisk4j.IYanDisk#delete(NodePath, boolean) delete} operations.
 * </p>
 */
@ApiStatus.NonExtendable
public sealed interface Operation extends Watchable<Operation> permits TerminalOperation, LiveOperation {

    Operation SUCCESS = new TerminalOperation(OperationStatus.SUCCESS);

    Operation FAILED = new TerminalOperation(OperationStatus.FAILED);

    static @NotNull Operation pending(@NotNull JsonObject object) {
        return new LiveOperation(NodeAwaiter.fromJson(object));
    }

    //

    /**
     * Returns the status of the operation. This method does not block, requests are made on a worker thread.
     */
    @NotNull OperationStatus status() throws YanDiskException;

    /**
     * Alias for {@code status().isComplete()}
     * @see #status()
     */
    @Override
    default boolean isDone() {
        return this.status().isComplete();
    }

    /**
     * Adds a callback to execute when the status of this operation changes. Callbacks are cleared when the operation
     * completes.
     * @see #status()
     */
    @Override
    void watch(@NotNull Consumer<Operation> callback);

    /**
     * Returns the {@link #setRefreshInterval(long) refresh interval}. Default value is 1 second (1000).
     */
    long getRefreshInterval();

    /**
     * Sets the refresh interval (time between updates by the daemon). The daemon starts at the first
     * call to {@link #status()} or {@link #watch(Consumer)}, and ends when the operation completes.
     * @param refreshInterval A duration, in milliseconds.
     */
    void setRefreshInterval(long refreshInterval);

}
