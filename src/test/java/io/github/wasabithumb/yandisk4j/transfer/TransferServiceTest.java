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
                () -> new ThrottledInputStream(new ByteArrayInputStream(sentinel), 8L),
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

    private static class ThrottledInputStream extends InputStream {

        private final InputStream in;
        private final long nsPerByte;
        private long timestamp;
        private long carry;
        ThrottledInputStream(InputStream in, long kbps) {
            this.in = in;
            this.nsPerByte = Math.floorDiv(1000000L, kbps);
            this.timestamp = System.nanoTime();
            this.carry = 0;
        }

        private long allowed() {
            long now = System.nanoTime();
            long elapsed = now - this.timestamp;
            if (elapsed < this.nsPerByte) {
                long wait = this.nsPerByte - elapsed;
                try {
                    TimeUnit.NANOSECONDS.sleep(wait);
                } catch (InterruptedException ignored) { }
                this.timestamp = now + wait;
                return this.carry + 1L;
            } else {
                this.timestamp = now;
                return this.carry + Math.floorDiv(elapsed, this.nsPerByte);
            }
        }

        @Override
        public int available() throws IOException {
            int available = this.in.available();
            if (available == 0) return 0;
            return (int) Math.min(available, this.allowed());
        }

        @Override
        public int read() throws IOException {
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

        @Override
        public void close() throws IOException {
            this.in.close();
        }

    }

}