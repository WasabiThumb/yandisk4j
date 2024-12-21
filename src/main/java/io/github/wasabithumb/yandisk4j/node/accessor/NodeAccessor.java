package io.github.wasabithumb.yandisk4j.node.accessor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Common values for an upload/download operation.
 */
@ApiStatus.NonExtendable
public sealed interface NodeAccessor permits AbstractNodeAccessor {

    @NotNull String href();

    @NotNull String method();

}
