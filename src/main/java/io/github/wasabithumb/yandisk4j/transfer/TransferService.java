package io.github.wasabithumb.yandisk4j.transfer;

import io.github.wasabithumb.yandisk4j.except.YanDiskException;
import io.github.wasabithumb.yandisk4j.except.YanDiskIOException;
import io.github.wasabithumb.yandisk4j.util.IDVendor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Internal
public class TransferService {

    private final ExecutorService executor;
    public TransferService(final @NotNull String descriptor) {
        final IDVendor vendor = new IDVendor();
        this.executor = Executors.newCachedThreadPool((Runnable r) -> new Runner(r, descriptor, vendor));
    }

    //

    private void submit(
            @NotNull Transfer transfer,
            @NotNull Callable<InputStream> input,
            @NotNull Callable<OutputStream> output
    ) {
        this.executor.submit(new Task(transfer, input, output));
    }

    public @NotNull Transfer submit(
            @NotNull Callable<InputStream> input,
            @NotNull Callable<OutputStream> output,
            @Range(from = -1L, to = Long.MAX_VALUE) long bytes
    ) {
        Transfer t = Transfer.create(bytes);
        this.submit(t, input, output);
        return Transfer.seal(t);
    }

    public @NotNull Transfer submit(
            @NotNull Callable<InputStream> input,
            @NotNull Callable<OutputStream> output
    ) {
        Transfer t = Transfer.create();
        this.submit(t, input, output);
        return Transfer.seal(t);
    }

    //

    private static class Runner extends Thread {

        private final IDVendor vendor;
        private final int id;
        Runner(@NotNull Runnable task, @NotNull String descriptor, @NotNull IDVendor vendor) {
            super(task);
            this.vendor = vendor;
            this.id = vendor.next();
            this.setName("YanDisk " + descriptor + " Thread #" + (this.id + 1));
        }

        @Override
        public void run() {
            try {
                super.run();
            } finally {
                this.vendor.free(this.id);
            }
        }

    }

    //

    private record Task(
            @NotNull Transfer transfer,
            @NotNull Callable<InputStream> input,
            @NotNull Callable<OutputStream> output
    ) implements Runnable {

        @Override
        public void run() {
            try {
                this.run0();
            } catch (YanDiskException e) {
                this.transfer.raise(e);
            } catch (IOException e) {
                this.transfer.raise(new YanDiskIOException("Broken pipe", e));
            } finally {
                this.transfer.update(-1L);
            }
        }

        private void run0() throws IOException {
            try (InputStream is = this.input.call();
                 OutputStream os = this.output.call()
            ) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf, 0, 8192)) != -1) {
                    os.write(buf, 0, read);
                    this.transfer.update(read);
                }
            } catch (IOException | YanDiskException e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionError("Provider threw an illegal exception", e);
            }
        }

    }

}
