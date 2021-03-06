package com.hazelcast.util.executor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class CancellableDelegatingFutureTest extends HazelcastTestSupport {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testInnerFutureThrowsCancellationExceptionWhenOuterFutureIsCancelled() throws Exception {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance instance = factory.newHazelcastInstance();
        IExecutorService executorService = instance.getExecutorService(randomString());
        final CompletesOnInterruptionCallable callable = new CompletesOnInterruptionCallable();
        final DelegatingFuture<Boolean> future = (DelegatingFuture<Boolean>) executorService.submit(callable);

        if (future.cancel(true)) {
            expected.expect(CancellationException.class);
            future.getFuture().get();
        }
    }

    static class CompletesOnInterruptionCallable implements Callable<Boolean>, Serializable {

        @Override
        public Boolean call() throws Exception {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return Boolean.TRUE;
                }
            }
        }
    }
}
