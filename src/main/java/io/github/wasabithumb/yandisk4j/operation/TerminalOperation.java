package io.github.wasabithumb.yandisk4j.operation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Internal
record TerminalOperation(
        @NotNull OperationStatus status
) implements Operation {

    @Override
    public void watch(@NotNull Consumer<Operation> callback) { }

    @Override
    public long getRefreshInterval() {
        return -1L;
    }

    @Override
    public void setRefreshInterval(long refreshInterval) { }

}
