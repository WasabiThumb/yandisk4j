package io.github.wasabithumb.yandisk4j.util;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PaginatedResult<T> implements Iterable<T> {

    private final Operation<T> operation;
    private final int limit;

    @ApiStatus.Internal
    public PaginatedResult(@NotNull Operation<T> operation, int limit) {
        this.operation = operation;
        this.limit = limit;
    }

    /**
     * Reports the limit (maximum number of elements in a page) for this query.
     */
    public int limit() {
        return this.limit;
    }

    /**
     * Fetches the nth page of the query (starts at 0).
     */
    public @NotNull List<T> get(int page) throws YanDiskException {
        return this.operation.execute(this.limit, this.limit * page);
    }

    /**
     * Returns an iterator which joins together pages to provide a continuous view of the source data.
     * Due to the nature of this operation, usage of the iterator may sneakily throw
     * {@link YanDiskException}.
     */
    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iter<>(this);
    }

    //

    @FunctionalInterface
    public interface Operation<Q> {

        @NotNull
        List<Q> execute(int limit, int offset) throws YanDiskException;

    }

    @ApiStatus.Internal
    static final class Iter<Q> implements Iterator<Q> {

        private final PaginatedResult<Q> src;
        private List<Q> data;
        private int dataIndex;
        private int head;
        private int subHead;
        Iter(@NotNull PaginatedResult<Q> src) {
            this.src = src;
            this.data = null;
            this.dataIndex = -1;
            this.head = 0;
            this.subHead = 0;
        }

        private @NotNull List<Q> data() {
            if (this.head != this.dataIndex) {
                this.data = this.src.get(this.head);
                this.dataIndex = this.head;
            }
            return this.data;
        }

        @Override
        public synchronized boolean hasNext() {
            List<Q> data = this.data();
            while (true) {
                if (this.subHead < data.size()) return true;
                if (data.size() < this.src.limit) return false;
                this.head++;
                this.subHead = 0;
                data = this.data();
            }
        }

        @Override
        public synchronized Q next() {
            List<Q> data = this.data();
            while (true) {
                if (this.subHead < data.size()) {
                    return data.get(this.subHead++);
                }
                if (data.size() < this.src.limit) throw new NoSuchElementException();
                this.head++;
                this.subHead = 0;
                data = this.data();
            }
        }

    }

}
