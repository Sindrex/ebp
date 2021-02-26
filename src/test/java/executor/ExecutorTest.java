package executor;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ExecutorTest {

    @Test
    public void submitTest() {
        Future<String> res = (Future<String>) Executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Test";
            }
        });

        try {
            String s = res.get();
            System.out.println(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}