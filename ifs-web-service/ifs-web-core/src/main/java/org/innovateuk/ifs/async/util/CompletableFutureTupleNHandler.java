package org.innovateuk.ifs.async.util;

import org.innovateuk.ifs.async.generation.AsyncFuturesGenerator;
import org.innovateuk.ifs.async.generation.AsyncFuturesHolder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A convenience subclass of {@link BaseCompletableFutureTupleHandler} that allows a developer to more concisely handle
 * the chaining of new CompletableFutures from the results of others.  It also handles the registering of the newly
 * chained Future with {@link AsyncFuturesHolder} to prevent the Controller from rendering any
 * templates until the new Future has completed.
 *
 * This subclass handles the chaining of more than 3 futures e.g in combination with
 * {@link AsyncFuturesGenerator#awaitAll(CompletableFuture,CompletableFuture,CompletableFuture,CompletableFuture...)}:
 *
 * <pre>
 * {@code
 *
 * awaitAll(future1, future2, future3, future4, future5).thenApply(() -> doSomething());
 * }
 * </pre>
 *
 * This subclass is slightly less generous than other {@link BaseCompletableFutureTupleHandler} subclasses in that it
 * does not supply the results of the futures explicitly to the handler methods passed to
 * {@link CompletableFutureTupleNHandler#thenAccept(Runnable)} or
 * {@link CompletableFutureTupleNHandler#thenApply(Supplier)}
 */
public class CompletableFutureTupleNHandler extends BaseCompletableFutureTupleHandler {

    public CompletableFutureTupleNHandler(List<CompletableFuture<?>> futures) {
        this(UUID.randomUUID().toString(), futures);
    }

    public CompletableFutureTupleNHandler(String futureName, List<CompletableFuture<?>> futures) {
        super(futureName, futures.toArray(new CompletableFuture[] {}));
    }

    public <R> CompletableFuture<R> thenApply(Supplier<R> supplier) {
        return thenApplyInternal(supplier::get);
    }

    public <R> CompletableFuture<Void> thenAccept(Runnable runnable) {

        Supplier<Void> dummySupplier = () -> {
            runnable.run();
            return null;
        };

        return thenApply(dummySupplier);
    }

    public void thenReturn() {
        waitForFuturesAndDescendantsToFullyComplete();
    }
}
