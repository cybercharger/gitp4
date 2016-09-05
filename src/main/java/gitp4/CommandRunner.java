package gitp4;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    private static final Logger logger = Logger.getLogger(CommandRunner.class);
    private static final long BUFFER_READ_INTERVAL = 100;
    private static void readStream(BufferedReader reader, List<String> result) throws IOException {
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            result.add(line);
        }
    }

    public static List<String> runCommand(String cmd) throws IOException, InterruptedException, ExecutionException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader stdReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        List<String> result = new LinkedList<>();
        List<String> error = new LinkedList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future stdFuture = executor.submit(() -> {
            try {
                readStream(stdReader, result);
            } catch (IOException e) {
                logger.error(e);
            }
        });

        Future errFuture = executor.submit(() -> {
            try {
                readStream(errReader, error);
            } catch (IOException e) {
                logger.error(e);
            }
        });
        stdFuture.get();
        errFuture.get();
        executor.shutdown();
        executor.awaitTermination(-1, TimeUnit.MILLISECONDS);
        p.waitFor();
        stdReader.close();
        errReader.close();

//        //Read cmd output every BUFFER_READ_INTERVAL milliseconds to prevent the process hung due to the buffer full
//        int i = 0;
//        for (; !p.waitFor(BUFFER_READ_INTERVAL, TimeUnit.MILLISECONDS); ++i) {
//            logger.debug("timeout for " + p.toString());
//            for ( String res = stdReader.readLine(); res != null; res = stdReader.readLine()) {
//                result.add(res);
//            }
//            logger.debug("all stdout read");
//            for (String err = errReader.readLine(); err != null; err = errReader.readLine()) {
//                error.add(err);
//            }
//        }
//        logger.debug("Process finished " + p.toString());
//        int j = 0;
//        for (String res = stdReader.readLine(); res != null; res = stdReader.readLine(), ++j) {
//            result.add(res);
//        }
//        for (String err = errReader.readLine(); err != null; err = stdReader.readLine()) {
//            error.add(err);
//        }
//        logger.debug(String.format("read buffer (%1$d + %2$d) times", i, j));
        if (!error.isEmpty()) {
            logger.debug(String.format("[Error or Warning] of running %1$s\n%2$s", cmd, StringUtils.join(error, "\n")));
        }
        return result;
    }
}
