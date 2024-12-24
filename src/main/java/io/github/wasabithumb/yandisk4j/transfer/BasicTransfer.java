package io.github.wasabithumb.yandisk4j.transfer;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@ApiStatus.Internal
class BasicTransfer implements Transfer {

    private final ReadWriteLock lock;
    private final List<Consumer<Transfer>> callbacks;
    private final long total;
    private long transferred;
    private boolean done;
    private YanDiskException error;
    BasicTransfer(long total) {
        this.lock = new ReentrantReadWriteLock();
        this.callbacks = new LinkedList<>();
        this.total = total;
        this.transferred = 0L;
        this.done = false;
        this.error = null;
    }

    @Override
    public @Range(from = 0L, to = Long.MAX_VALUE) long transferred() {
        this.lock.readLock().lock();
        try {
            return this.transferred;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public @Range(from = -1L, to = Long.MAX_VALUE) long total() {
        return this.total;
    }

    @Override
    public long update(@Range(from = -1L, to = Long.MAX_VALUE) long bytes) {
        if (bytes == 0L) return 0L;

        List<Consumer<Transfer>> callbacks = this.callbacks;
        boolean unmoved = true;

        this.lock.writeLock().lock();
        try {
            if (bytes == -1L) {
                if (!this.done) {
                    callbacks = new ArrayList<>(callbacks);
                    unmoved = false;
                    this.callbacks.clear();
                }
                this.done = true;
            } else {
                this.transferred += bytes;
            }
        } finally {
            this.lock.writeLock().unlock();
        }

        if (unmoved) this.lock.readLock().lock();
        try {
            for (Consumer<Transfer> callback : callbacks)
                callback.accept(this);
        } finally {
            if (unmoved) this.lock.readLock().unlock();
        }
        return bytes;
    }

    @Override
    public @Nullable YanDiskException error() {
        this.lock.readLock().lock();
        try {
            return this.error;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void raise(@NotNull YanDiskException error) {
        this.lock.writeLock().lock();
        try {
            if (this.error != null) error.addSuppressed(this.error);
            this.error = error;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void watch(@NotNull Consumer<Transfer> callback) {
        this.lock.writeLock().lock();
        try {
            if (this.done) return;
            this.callbacks.add(callback);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isDone() {
        this.lock.readLock().lock();
        try {
            return this.done;
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
