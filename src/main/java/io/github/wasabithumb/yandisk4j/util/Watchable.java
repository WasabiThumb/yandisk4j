package io.github.wasabithumb.yandisk4j.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A resource that may have progress, can be listened to, and will complete in the future.
 * @see #watch(Consumer)
 * @see #isDone()
 * @see #progress()
 * @see #hasProgress()
 */
public interface Watchable<W extends Watchable<W>> {

    /**
     * Adds a mutation callback to this resource. This is called any time the resource changes.
     * Callbacks are cleared after the resource {@link #isDone() completes}.
     */
    void watch(@NotNull Consumer<W> callback);

    /**
     * Returns true if the resource is complete (will no longer change).
     */
    boolean isDone();

    /**
     * Provides a value from 0 to 1 inclusive, indicating the progress of this resource.
     * @throws UnsupportedOperationException The resource has no progress information (see {@link #hasProgress()})
     */
    default double progress() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Watchable has no progress information");
    }

    /**
     * Returns true if this resource has progress information.
     * @see #progress()
     */
    default boolean hasProgress() {
        return false;
    }

}
