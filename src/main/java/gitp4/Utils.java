package gitp4;

import gitp4.git.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by chriskang on 9/1/2016.
 */
public class Utils {

    public static boolean isValidGitCommitId(String commitId) {
        return !StringUtils.isBlank(commitId) &&
                (Constants.fullCommitIdPattern.matcher(commitId).matches()
                        || Constants.abbrCommitIdPattern.matcher(commitId).matches());
    }

    public static <T> List<T> runConcurrently(int nThreads,
                                              Collection<Callable<T>> theCallable,
                                              Consumer<T> onEachDone)
            throws InterruptedException, ExecutionException {
        if (nThreads < 0) throw new IllegalArgumentException("nThread < 0");
        if (theCallable == null || theCallable.isEmpty()) throw new NullPointerException("theCallable");
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        List<Future<T>> futures = executor.invokeAll(theCallable);
        List<T> result = new ArrayList<>(theCallable.size());
        for (Future<T> f : futures) {
            T res = f.get();
            result.add(res);
            if (onEachDone != null) {
                try {
                    onEachDone.accept(res);
                } catch (Exception e) {
                    Logger.getLogger(Utils.class).error(e);
                }
            }
        }
        executor.shutdown();
        executor.awaitTermination(-1, TimeUnit.MILLISECONDS);
        return result;
    }

    public static Boolean runConcurrentlyAndAggregate(int nThreads,
                                                      Collection<Callable<Boolean>> theCallable,
                                                      Consumer<Boolean> onEachDone)
            throws ExecutionException, InterruptedException {
        List<Boolean> result = runConcurrently(nThreads, theCallable, onEachDone);
        for(Boolean b : result) {
            if (!b) return false;
        }
        return true;
    }
}
