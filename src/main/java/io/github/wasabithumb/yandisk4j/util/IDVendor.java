package io.github.wasabithumb.yandisk4j.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.BitSet;

@ApiStatus.Internal
public class IDVendor {

    private final int initialCapacity;
    private int capacity;
    private BitSet data;
    public IDVendor(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        this.capacity = initialCapacity;
        this.data = new BitSet(initialCapacity);
    }

    public IDVendor() {
        this(64);
    }

    public synchronized int next() {
        int clear = this.data.nextClearBit(0);
        int ret;
        if (clear == this.capacity) {
            ret = this.capacity;
            int newCapacity = this.capacity << 1;
            this.data = new BitSet(newCapacity);
            this.data.set(0, this.capacity + 1);
            this.capacity = newCapacity;
        } else {
            ret = clear;
            this.data.set(clear);
        }
        return ret;
    }

    public synchronized void free(int index) {
        if (index < 0 || index >= this.capacity)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for capacity " + this.capacity);
        this.data.clear(index);
        if (this.capacity > this.initialCapacity && this.data.nextSetBit(this.initialCapacity) == -1) {
            BitSet shrink = new BitSet(this.initialCapacity);
            for (int i=0; i < this.initialCapacity; i++) {
                if (this.data.get(i)) shrink.set(i);
            }
            this.data = shrink;
            this.capacity = initialCapacity;
        }
    }

}
