package org.innovateuk.ifs.async.config;

import org.innovateuk.ifs.async.generation.AsyncFuturesGenerator;
import org.innovateuk.ifs.commons.BaseIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.innovateuk.ifs.util.CollectionFunctions.combineLists;
import static org.junit.Assert.assertEquals;

/**
 * Tests the important concepts of the Async mechanism
 */
@TestPropertySource(properties = "ifs.web.ajp.connections.max.total=3")
public class AsyncMechanismIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AsyncFuturesGenerator asyncGenerator;

    /**
     * This test asserts that Future code is executed with the Thread Pool configured in
     * {@link org.innovateuk.ifs.async.config.AsyncThreadPoolTaskExecutorConfig} when executed from our
     * {@link AsyncFuturesGenerator#async(org.innovateuk.ifs.util.ExceptionThrowingRunnable) entrypoint}
     *
     */
    @Test
    public void testAsyncJobsAreExecutedByOurAsyncExecutorFactory() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = asyncGenerator.async(() -> Thread.currentThread().getName());
        String futureThreadName = future.get();
        assertThat(futureThreadName, startsWith("IFS-Async-Executor-"));
    }

    /**
     * This test tests that parallel Futures will be invoked in new Threads from the pool in ever-incrementing numbers
     */
    @Test
    public void testAsyncJobsAreExecutedByOurAsyncExecutorFactoryAndUseIncrementalConnections() throws ExecutionException, InterruptedException {

        CompletableFuture<List<String>> future = asyncGenerator.async(() -> {
            CompletableFuture<List<String>> future2 = asyncGenerator.async(() -> {
                CompletableFuture<String> future3 = asyncGenerator.async(() -> Thread.currentThread().getName());
                String futureThreadName = future3.get();
                return asList(Thread.currentThread().getName(), futureThreadName);
            });
            List<String> futureThreadNames = future2.get();
            return combineLists(Thread.currentThread().getName(), futureThreadNames);
        });

        List<String> threadNames = future.get();
        String firstThreadName = threadNames.get(0);
        String secondThreadName = threadNames.get(1);
        String thirdThreadName = threadNames.get(2);

        int firstThreadNumber = Integer.valueOf(firstThreadName.substring(firstThreadName.lastIndexOf('-') + 1));

        assertEquals("IFS-Async-Executor-" + firstThreadNumber, firstThreadName);
        assertEquals("IFS-Async-Executor-" + nextThreadNumber(firstThreadNumber + 1), secondThreadName);
        assertEquals("IFS-Async-Executor-" + nextThreadNumber(firstThreadNumber + 2), thirdThreadName);
    }

    /**
     * We've specified in this test class a 3 Thread limit for our Thread Pool.  Therefore once Thread 3 is used, the
     * next Thread to be selected will be Thread 1 again.
     */
    private int nextThreadNumber(int number) {
        return ((number - 1) % 3) + 1;
    }

}
