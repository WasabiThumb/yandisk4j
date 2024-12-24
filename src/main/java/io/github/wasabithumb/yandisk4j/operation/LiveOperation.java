package io.github.wasabithumb.yandisk4j.operation;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.except.YanDiskOperationException;
import io.github.wasabithumb.yandisk4j.node.accessor.NodeAwaiter;
import io.github.wasabithumb.yandisk4j.util.IDVendor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

@ApiStatus.Internal
final class LiveOperation implements Operation {

    private final NodeAwaiter awaiter;
    private final StampedLock statusLock = new StampedLock();
    private final List<Consumer<Operation>> statusCallbacks = Collections.synchronizedList(new LinkedList<>());
    private OperationStatus status = OperationStatus.PENDING;
    private YanDiskException exception = null;
    private long refreshInterval = 1000L;
    private Daemon daemon = null;

    public LiveOperation(@NotNull NodeAwaiter awaiter) {
        this.awaiter = awaiter;
    }

    //

    private synchronized void initDaemon() {
        if (this.daemon != null) return;
        this.daemon = new Daemon(this);
        this.daemon.start();
    }

    @Override
    public @NotNull OperationStatus status() throws YanDiskException {
        this.initDaemon();
        final long stamp = this.statusLock.readLock();
        try {
            if (this.exception != null) throw this.exception;
            return this.status;
        } finally {
            this.statusLock.unlock(stamp);
        }
    }

    @Override
    public void watch(@NotNull Consumer<Operation> callback) {
        this.initDaemon();
        final long stamp = this.statusLock.readLock();
        try {
            if (this.status.isComplete()) return;
            this.statusCallbacks.add(callback);
        } finally {
            this.statusLock.unlock(stamp);
        }
    }

    @Override
    public long getRefreshInterval() {
        return this.refreshInterval;
    }

    @Override
    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    private void updateStatus(@NotNull OperationStatus status) {
        if (!status.isComplete()) throw new IllegalArgumentException();
        List<Consumer<Operation>> callbacks;

        long stamp = this.statusLock.readLock();
        try {
            if (this.status.isComplete()) return;
            stamp = this.statusLock.tryConvertToWriteLock(stamp);
            this.status = status;
            synchronized (this.statusCallbacks) {
                callbacks = new ArrayList<>(this.statusCallbacks);
                this.statusCallbacks.clear();
            }
        } finally {
            this.statusLock.unlock(stamp);
        }

        for (Consumer<Operation> callback : callbacks)
            callback.accept(this);
    }

    private void addException(@NotNull YanDiskException e) {
        long stamp = this.statusLock.writeLock();
        try {
            if (this.exception != null) e.addSuppressed(this.exception);
            this.exception = e;
        } finally {
            this.statusLock.unlock(stamp);
        }
    }

    //

    private static final class Daemon extends Thread {

        private static final IDVendor ID_VENDOR = new IDVendor();

        private final int id;
        private final LiveOperation operation;
        Daemon(@NotNull LiveOperation operation) {
            this.id = ID_VENDOR.next();
            this.operation = operation;
            this.setName("YanDisk Operation Daemon #" + (this.id + 1));
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                OperationStatus status;
                try {
                    status = this.runInternal();
                } catch (IOException e) {
                    this.operation.addException(new YanDiskIOException(e));
                    status = OperationStatus.FAILED;
                } catch (YanDiskException e) {
                    this.operation.addException(e);
                    status = OperationStatus.FAILED;
                }
                this.operation.updateStatus(status);
            } finally {
                ID_VENDOR.free(this.id);
            }
        }

        private @NotNull OperationStatus runInternal() throws YanDiskException, IOException {
            while (true) {
                String code = this.operation.awaiter.getStatus();
                OperationStatus status = switch (code) {
                    case "success" -> OperationStatus.SUCCESS;
                    case "failed" -> OperationStatus.FAILED;
                    case "in-progress" -> OperationStatus.PENDING;
                    default -> throw new YanDiskOperationException("Invalid status code: " + code);
                };
                if (status.isComplete()) {
                    return status;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(this.operation.refreshInterval);
                } catch (InterruptedException e) {
                    throw new YanDiskOperationException("Daemon interrupted");
                }
            }
        }

    }

}
