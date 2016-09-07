package gitp4;

import gitp4.console.Progress;
import gitp4.git.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by chriskang on 9/1/2016.
 */
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static boolean isValidGitCommitId(String commitId) {
        return !StringUtils.isBlank(commitId) &&
                (Constants.fullCommitIdPattern.matcher(commitId).matches()
                        || Constants.abbrCommitIdPattern.matcher(commitId).matches());
    }

    public static <T> List<T> runConcurrently(int nThreads,
                                              Collection<Callable<T>> theCallable)
            throws InterruptedException, ExecutionException {
        if (nThreads < 0) throw new IllegalArgumentException("nThread < 0");
        if (theCallable == null || theCallable.isEmpty()) throw new NullPointerException("theCallable");
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        List<Future<T>> futures = executor.invokeAll(theCallable);
        List<T> result = new ArrayList<>(theCallable.size());
        for (Future<T> f : futures) {
            T res = f.get();
            result.add(res);
        }
        executor.shutdown();
        executor.awaitTermination(-1, TimeUnit.MILLISECONDS);
        return result;
    }

    public static Boolean runConcurrentlyAndAggregate(int nThreads,
                                                      Collection<Callable<Boolean>> theCallable)
            throws ExecutionException, InterruptedException {
        List<Boolean> result = runConcurrently(nThreads, theCallable);
        for (Boolean b : result) {
            if (!b) return false;
        }
        return true;
    }


    public static <T> void pagedAction(List<T> data, final int pageSize, Consumer<List<T>> action) throws Exception {
        Progress p = new Progress(data.size());
        int page = 0;
        p.show();
        for (; page < data.size() / pageSize; ++page) {
            List<T> section = new ArrayList<>(pageSize);
            for (int i = 0; i < pageSize; ++i) {
                section.add(data.get(i + page * pageSize));
            }
            action.accept(section);
            p.progress(pageSize);
        }

        List<T> section = new ArrayList<>();
        for (int i = pageSize * page; i < data.size(); ++i) {
            section.add(data.get(i));
        }
        if (!section.isEmpty()) {
            action.accept(section);
            p.progress(data.size() - page * pageSize);
        }
    }

    //File.mkdirs will treat file starting with . as dir, that's why we need this
    public static void createDirRecursively(String filePath, char delimiter, Consumer<IOException> onIOException) {
        if (StringUtils.isBlank(filePath)) throw new NullPointerException("filePath");
        if (onIOException == null) throw new NullPointerException("onIOException");
        String[] dirs = StringUtils.split(filePath, "" + delimiter);
        String dirStr = dirs[0];
        // the last section is file name itself
        for (int i = 1; i < dirs.length - 1; ++i) {
            dirStr = "".equals(dirStr) ? dirs[i] : String.format("%1$s%2$c%3$s", dirStr, delimiter, dirs[i]);
            Path wanted = Paths.get(dirStr);
            if (!Files.exists(wanted)) {
                try {
                    Files.createDirectory(wanted);
                } catch (IOException e) {
                    logger.error("Failed to create dir: " + dirStr);
                    onIOException.accept(e);
                }
                logger.debug("dirs created: " + dirStr);
            }
        }
    }

    public static <T> T runtimeExceptionWrapper(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            logger.error(e);
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
    }
}
