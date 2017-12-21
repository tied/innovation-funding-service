package org.innovateuk.ifs.async.controller;

import org.junit.Test;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test for the thread safety of a {@link org.springframework.ui.Model} wrapped in a {@link ThreadsafeModel} wrapper.
 */
public class ThreadsafeModelTest {

    @Test
    public void testCallsToContainsAttributeDoNotLockOtherReadOperations() throws ExecutionException, InterruptedException {

        assertFirstReadOperationDoesNotBlockSecondReadOperation(
                model -> model.containsAttribute("read1"),
                model -> model.containsAttribute("read2"));

        assertFirstReadOperationDoesNotBlockSecondReadOperation(
                model -> model.containsAttribute("read1"), Model::asMap);
    }

    @Test
    public void testCallsToAsMapDoNotLockOtherReadOperations() throws ExecutionException, InterruptedException {

        assertFirstReadOperationDoesNotBlockSecondReadOperation(
                Model::asMap, model -> model.containsAttribute("read2"));
    }

    @Test
    public void testCallsToContainsAttributeLocksWriteAccessUntilComplete() throws ExecutionException, InterruptedException {

        assertReadOperationBlocksWriteOperation(
                model -> model.containsAttribute("read1"),
                model -> model.addAttribute("write2"));

        assertReadOperationBlocksWriteOperation(
                Model::asMap,
                model -> model.addAllAttributes(Collections.singleton("write2")));

        assertReadOperationBlocksWriteOperation(
                Model::asMap,
                model -> model.mergeAttributes(asMap(1, 2)));
    }

    /**
     * This test tests that we can have as many concurrent reads as we like whilst maintaing thread safety.
     *
     * This test issues 2 calls to {@link ThreadsafeModel#containsAttribute(String)}, the first of which blocks *within*
     * the bounds of the threadsafe locking mechanism inside ThreadsafeModel until the 2nd call is executed, upon which
     * the first call is unblocked.  The test performs this sequence in such a way so as to allow the 2nd call to write
     * its value to the "operationValues" list before the 1st call does, and we can assert that the 2nd value always makes it
     * into the list before the first does.
     *
     * This test proves therefore that a 2nd "read" operation can occur whilst a first "read" operation is in progress
     * (simulated by blocking the 1st call on the countDownLatch until the 2nd one is ready to release it).
     */
    private void assertFirstReadOperationDoesNotBlockSecondReadOperation(Consumer<Model> read1Operation, Consumer<Model> read2Operation) throws InterruptedException, ExecutionException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Model wrappedModel = mock(Model.class);

        ThreadsafeModel threadsafeModel = new ThreadsafeModel(wrappedModel);

        List<String> operationValues = new ArrayList<>();

        Model read1WhenAnswer = doAnswer(invocation -> {
            countDownLatch.await();
            operationValues.add("read1 operation");
            return null;
        }).when(wrappedModel);

        read1Operation.accept(read1WhenAnswer);

        Model read2WhenAnswer = doAnswer(invocation -> {
            operationValues.add("read2 operation");
            countDownLatch.countDown();
            return null;
        }).when(wrappedModel);

        read2Operation.accept(read2WhenAnswer);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> read1Future = executor.submit(() -> read1Operation.accept(threadsafeModel));
        Future<?> read2Future = executor.submit(() -> read2Operation.accept(threadsafeModel));

        read1Future.get();
        read2Future.get();

        Model read1Verification = verify(wrappedModel, atLeastOnce());
        read1Operation.accept(read1Verification);

        Model read2Verification = verify(wrappedModel, atLeastOnce());
        read2Operation.accept(read2Verification);

        assertEquals("read2 operation", operationValues.get(0));
        assertEquals("read1 operation", operationValues.get(1));
    }

    /**
     * This test tests that a read operation will block access to the wrapped {@link Model} write methods until they have finished.
     *
     * The test initiates both a read operation and a write operation at the same time, but ensures that the read operation will
     * make it into the locking boundary of {@link ThreadsafeModel} before the write operation is allowed to properly begin (by
     * use of the writeCountDownLatch).
     *
     * The read operation will then wait 100ms before writing its value to the "operationValues" list.  This should be the first
     * item of the list every time because the write operation cannot write its own value to the list until the read operation is
     * complete.  Therefore it has been successfully blocked for the 100ms wait time of the read operation.
     */
    private void assertReadOperationBlocksWriteOperation(Consumer<Model> readOperation, Consumer<Model> writeOperation) throws InterruptedException, ExecutionException {

        CountDownLatch readCountDownLatch = new CountDownLatch(1);
        CountDownLatch writeCountDownLatch = new CountDownLatch(1);

        Model wrappedModel = mock(Model.class);

        ThreadsafeModel threadsafeModel = new ThreadsafeModel(wrappedModel);

        List<String> operationValues = new ArrayList<>();

        Model readWhenAnswer = doAnswer(invocation -> {
            writeCountDownLatch.countDown();
            readCountDownLatch.await(200, TimeUnit.MILLISECONDS);
            operationValues.add("read operation");
            return null;
        }).when(wrappedModel);

        readOperation.accept(readWhenAnswer);

        Model writeWhenAnswer = doAnswer(invocation -> {
            operationValues.add("write operation");
            return null;
        }).when(wrappedModel);

        writeOperation.accept(writeWhenAnswer);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> readFuture = executor.submit(() -> readOperation.accept(threadsafeModel));

        Future<?> writeFuture = executor.submit(() -> {
            writeCountDownLatch.await();
            writeOperation.accept(threadsafeModel);
            return null;
        });

        readFuture.get();
        writeFuture.get();

        Model readVerification = verify(wrappedModel);
        readOperation.accept(readVerification);

        Model writeVerification = verify(wrappedModel);
        writeOperation.accept(writeVerification);

        assertEquals("read operation", operationValues.get(0));
        assertEquals("write operation", operationValues.get(1));
    }
}
