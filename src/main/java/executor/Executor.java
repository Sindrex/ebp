package executor;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * This class executes background tasks using a thread pool.
 */
public class Executor {
    private static final int MAX_THREADS = 10;
    private final static ExecutorService executorService = Executors.newScheduledThreadPool(MAX_THREADS);

    public static Future<?> submit(Callable<String> r) {
        return executorService.submit(r);
    }
}
