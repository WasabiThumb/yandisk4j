package io.github.wasabithumb.yandisk4j.operation;

public enum OperationStatus {
    PENDING,
    SUCCESS,
    FAILED;

    /**
     * Returns true if this status is {@link #PENDING}.
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Returns true if this status is either {@link #SUCCESS} or {@link #FAILED}.
     */
    public boolean isComplete() {
        return !this.isPending();
    }

}
