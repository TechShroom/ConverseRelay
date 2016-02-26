package me.kenzierocks.converse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.common.util.concurrent.MoreExecutors;

public final class OhNoMoreFutures {

    public static <V> void whenCompleted(CompletableFuture<V> future,
            Consumer<V> consumer) {
        future.thenAcceptAsync(consumer, MoreExecutors.directExecutor());
    }

    private OhNoMoreFutures() {
        throw new AssertionError("Oh No! More Lemmings.");
    }

}
