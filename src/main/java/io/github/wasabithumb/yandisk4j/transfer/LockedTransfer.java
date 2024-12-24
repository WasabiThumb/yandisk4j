package io.github.wasabithumb.yandisk4j.transfer;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

@ApiStatus.Internal
class LockedTransfer implements Transfer {

    private final Transfer backing;
    LockedTransfer(@NotNull Transfer backing) {
        this.backing = backing;
    }

    @Override
    public @Range(from = 0L, to = Long.MAX_VALUE) long transferred() {
        return this.backing.transferred();
    }

    @Override
    public @Range(from = -1L, to = Long.MAX_VALUE) long total() {
        return this.backing.total();
    }

    @Override
    public void watch(@NotNull Consumer<Transfer> callback) {
        this.backing.watch((Transfer ignored) -> callback.accept(LockedTransfer.this));
    }

    @Override
    public boolean isDone() {
        return this.backing.isDone();
    }

    @Override
    public long update(@Range(from = -1L, to = Long.MAX_VALUE) long bytes) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot update LockedTransfer");
    }

    @Override
    public @Nullable YanDiskException error() {
        return this.backing.error();
    }

    @Override
    public void raise(@NotNull YanDiskException error) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot raise an exception on LockedTransfer");
    }

}
