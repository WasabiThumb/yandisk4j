package io.github.wasabithumb.yandisk4j.transfer;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

class TransferServiceTest {

    @Test
    void basic() {
        TransferService service = new TransferService("Test");

        final byte[] sentinel = new byte[16384]; // 16KB random data
        ThreadLocalRandom.current().nextBytes(sentinel);
        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        Transfer t;
        t = service.submit(
                () -> new ThrottleInputStream(new ByteArrayInputStream(sentinel), 200L),
                () -> dest,
                sentinel.length
        );

        t.watch(this::report);
        assertDoesNotThrow(t::block);

        final byte[] out = dest.toByteArray();
        assertArrayEquals(sentinel, out);
    }

    private void report(Transfer t) {
        if (t.isDone()) {
            YanDiskException error = t.error();
            if (error != null) throw error;
            System.out.println("= DONE =");
            return;
        }

        String progress = "??%";
        if (t.hasProgress()) {
            progress = (long) Math.floor(t.progress() * 100d) + "%";
        }
        System.out.println(t.transferred() + " / " + t.total() + " (" + progress + ")");
    }

    //

    private static class ThrottleInputStream extends InputStream {

        private final InputStream in;
        private final long usPerByte;
        private long timestamp;
        private long carry;
        ThrottleInputStream(InputStream in, long speed) {
            this.in = in;
            this.usPerByte = Math.floorDiv(1000L, speed);
            this.timestamp = System.nanoTime();
            this.carry = 0;
        }

        private long allowed() {
            long now = System.nanoTime();
            long elapsed = Math.floorDiv(now - this.timestamp, 1000L);
            if (elapsed < this.usPerByte) {
                try {
                    TimeUnit.MICROSECONDS.sleep(this.usPerByte - elapsed);
                } catch (InterruptedException ignored) { }
                this.timestamp = System.nanoTime();
                return this.carry + 1L;
            } else {
                this.timestamp = now;
                return this.carry + Math.floorDiv(elapsed, this.usPerByte);
            }
        }

        @Override
        public synchronized int read() throws IOException {
            long allowed = this.allowed();
            int ret = this.in.read();
            this.carry = allowed - 1L;
            return ret;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            long allowed = this.allowed();
            int count = (int) Math.min(len, allowed);
            count = Math.min(count, this.in.read(b, off, count));
            this.carry = allowed - count;
            return count;
        }

    }

}