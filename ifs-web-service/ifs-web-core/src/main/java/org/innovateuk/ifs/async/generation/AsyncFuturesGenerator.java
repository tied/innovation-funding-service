package org.innovateuk.ifs.async.generation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.async.util.CompletableFutureTuple1Handler;
import org.innovateuk.ifs.async.util.CompletableFutureTuple2Handler;
import org.innovateuk.ifs.async.util.CompletableFutureTuple3Handler;
import org.innovateuk.ifs.async.util.CompletableFutureTupleNHandler;
import org.innovateuk.ifs.util.ExceptionThrowingRunnable;
import org.innovateuk.ifs.util.ExceptionThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.util.CollectionFunctions.combineLists;

/**
 * The entry mechanism for executing blocks of code asynchronously.  All code that wishes to execute multiple jobs in
 * parallel should use this class as the first port of call.
 *
 * The {@link AsyncFuturesGenerator} async() method is the main entrypoint to spawn async blocks of code, and this will return
 * a CompletableFuture that can be waited on e.g. async(() -> doSomethingAsynchronously());
 *
 * Additionally, there are multiple awaitAll() methods provided here that, given X futures to wait on, will return
 * a convenience wrapper object (a CompletableFutureTupleXHandler) that will allow the developer to use a handy
 * thenApply() or thenAccept() method to chain additional Future behaviour.  This Future behaviour method is passed an
 * appropriate tuple of X parameters after all X awaited futures have completed e.g.
 *
 * awaitAll(future1, future2).thenApply((result1, result2) -> doSomethingWith(result1, result2));
 *
 * All CompletableFutures generated via the async() and awaitAll() methods provided in this class will be registered
 * with {@link AsyncFuturesHolder} which will prevent Controllers from rendering templates until all of the Futures have
 * completed.
 */
@Component
public class AsyncFuturesGenerator {

    private static final Log LOG = LogFactory.getLog(AsyncFuturesGenerator.class);

    /**
     * A self-wiring here to allow methods within this class to use the @Async proxy mechanisms put in place by Spring
     * on the {@link AsyncFuturesGenerator#asyncInternal} method.
     */
    private AsyncFuturesGenerator self;

    @Autowired
    @Lazy
    public AsyncFuturesGenerator(AsyncFuturesGenerator self) {
        this.self = self;
    }

    public <T> CompletableFuture<T> async(ExceptionThrowingSupplier<T> supplier) {
        return async(UUID.randomUUID().toString(), supplier);
    }

    public <T> CompletableFuture<T> async(String futureName, ExceptionThrowingSupplier<T> supplier) {

        AsyncFutureDetails currentlyExecutingFuture = AsyncFuturesHolder.getCurrentlyExecutingFutureDetails() != null ?
                AsyncFuturesHolder.getCurrentlyExecutingFutureDetails() :
                AsyncFutureDetails.topLevelThread();

        Supplier<T> decoratedSupplier = () -> {

            AsyncFuturesHolder.addCurrentFutureBeingProcessed(futureName, currentlyExecutingFuture);

            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                AsyncFuturesHolder.clearCurrentFutureBeingProcessed();
            }
        };

        CompletableFuture<T> asyncBlock = self.asyncInternal(decoratedSupplier);
        return AsyncFuturesHolder.registerFuture(futureName, asyncBlock);
    }

    public CompletableFuture<Void> async(ExceptionThrowingRunnable runnable) {
        return async(UUID.randomUUID().toString(), runnable);
    }

    public CompletableFuture<Void> async(String futureName, ExceptionThrowingRunnable runnable) {

        ExceptionThrowingSupplier<Void> dummySupplier = () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        return async(futureName, dummySupplier);
    }

    @Async
    <T> CompletableFuture<T> asyncInternal(Supplier<T> supplier) {
        return CompletableFuture.completedFuture(supplier.get());
    }

    public <R1> CompletableFutureTuple1Handler<R1> awaitAll(CompletableFuture<R1> future1) {
        return awaitAll(UUID.randomUUID().toString(), future1);
    }

    public <R1, R2> CompletableFutureTuple2Handler<R1, R2> awaitAll(CompletableFuture<R1> future1, CompletableFuture<R2> future2) {
        return awaitAll(UUID.randomUUID().toString(), future1, future2);
    }

    public <R1, R2, R3> CompletableFutureTuple3Handler<R1, R2, R3> awaitAll(CompletableFuture<R1> future1, CompletableFuture<R2> future2, CompletableFuture<R3> future3) {
        return awaitAll(UUID.randomUUID().toString(), future1, future2, future3);
    }

    public CompletableFutureTupleNHandler awaitAll(CompletableFuture<?> future1, CompletableFuture<?> future2, CompletableFuture<?> future3, CompletableFuture<?>... moreFutures) {
        return awaitAll(UUID.randomUUID().toString(), future1, future2, future3, moreFutures);
    }

    public <R1> CompletableFutureTuple1Handler<R1> awaitAll(String futureName, CompletableFuture<R1> future1) {
        return new CompletableFutureTuple1Handler<>(futureName, future1);
    }

    public <R1, R2> CompletableFutureTuple2Handler<R1, R2> awaitAll(String futureName, CompletableFuture<R1> future1, CompletableFuture<R2> future2) {
        return new CompletableFutureTuple2Handler<>(futureName, future1, future2);
    }

    public <R1, R2, R3> CompletableFutureTuple3Handler<R1, R2, R3> awaitAll(String futureName, CompletableFuture<R1> future1, CompletableFuture<R2> future2, CompletableFuture<R3> future3) {
        return new CompletableFutureTuple3Handler<>(futureName, future1, future2, future3);
    }

    public CompletableFutureTupleNHandler awaitAll(String futureName, CompletableFuture<?> future1, CompletableFuture<?> future2, CompletableFuture<?> future3, CompletableFuture<?>... moreFutures) {
        List<CompletableFuture<?>> allFutures = combineLists(asList(future1, future2, future3), moreFutures);
        return new CompletableFutureTupleNHandler(futureName, allFutures);
    }

    public <T> T futureResult(CompletableFuture<T> future) {
        return getQuietly(future);
    }

    private <T> T getQuietly(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception caught whilst awaiting a Future - rethrowing", e);
            throw new RuntimeException(e);
        }
    }
}