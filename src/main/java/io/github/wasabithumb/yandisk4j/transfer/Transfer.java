package io.github.wasabithumb.yandisk4j.transfer;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.util.Watchable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface Transfer extends Watchable<Transfer> {

    /**
     * Creates a new {@link Transfer}
     * @param total Total number of bytes expected to transfer, or -1 if no expectation
     */
    static @NotNull Transfer create(@Range(from = -1L, to = Long.MAX_VALUE) long total) {
        return new BasicTransfer(total);
    }

    /**
     * Creates a new {@link Transfer} with no total byte count. This transfer will be unable to calculate
     * its progress ({@link #hasProgress()} is false). Alias for {@code create(-1L)}.
     * @see #create()
     */
    static @NotNull Transfer create() {
        return create(-1L);
    }

    /**
     * Returns a view of the given {@link Transfer} that does not allow {@link #update(long)} to be called.
     */
    static @NotNull Transfer seal(@NotNull Transfer base) {
        if (base instanceof LockedTransfer locked) {
            return locked;
        }
        return new LockedTransfer(base);
    }

    //

    /**
     * The number of bytes transferred so far.
     */
    @Range(from = 0L, to = Long.MAX_VALUE) long transferred();

    /**
     * The number of bytes we expect to transfer, or -1 for no expectation (progress cannot be calculated).
     */
    @Range(from = -1L, to = Long.MAX_VALUE)
    long total();

    /**
     * Adds a mutation callback to this transfer. This is called whenever {@link #transferred()}/
     * {@link #progress()} changes or the transfer {@link #isDone() completes}.
     */
    @Override
    void watch(@NotNull Consumer<Transfer> callback);

    /**
     * Updates this transfer.
     * @param bytes The number of additional bytes, or -1 if transfer is complete.
     * @throws UnsupportedOperationException Transfer is immutable
     */
    long update(@Range(from = -1L, to = Long.MAX_VALUE) long bytes) throws UnsupportedOperationException;

    /**
     * Any errors that were raised as a result of the transfer. The transfer will always
     * {@link #isDone() complete}, this should be used to test whether the completion was exceptional.
     */
    @Nullable YanDiskException error();

    /**
     * Adds an {@link #error() error} to the transfer
     * @throws UnsupportedOperationException Transfer is immutable
     */
    void raise(@NotNull YanDiskException error) throws UnsupportedOperationException;

    /**
     * The progress, measured as {@link #transferred()} over {@link #total()}.
     * @throws UnsupportedOperationException {@link #total() Total} is not set (set to {@code -1}), making
     * {@link #hasProgress()} false.
     */
    @Override
    default double progress() throws UnsupportedOperationException {
        final long total = this.total();
        if (total == -1)
            throw new UnsupportedOperationException("Cannot calculate progress for Transfer with no set total");
        return ((double) Math.min(this.transferred(), total)) / ((double) total);
    }

    /**
     * Alias for {@code total() != -1L}
     * @see #progress()
     */
    @Override
    default boolean hasProgress() {
        return this.total() != -1L;
    }

    /**
     * Halts the execution of the calling thread until the transfer completes.
     */
    default void block() throws InterruptedException {
        final Object mutex = new Object();
        this.watch((Transfer t) -> {
            if (t.isDone()) {
                synchronized (mutex) {
                    mutex.notify();
                }
            }
        });
        synchronized (mutex) {
            if (this.isDone()) return;
            mutex.wait();
        }
    }

}
