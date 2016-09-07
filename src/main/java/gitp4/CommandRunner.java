package gitp4;

import gitp4.console.Progress;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    private static final Logger logger = Logger.getLogger(CommandRunner.class);
    private static final long BUFFER_READ_INTERVAL = 100;

    private static void readStream(BufferedReader reader, List<String> result, Process p) {
        try {
            while (true) {
                Thread.sleep(BUFFER_READ_INTERVAL);
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    result.add(line);
                }
                if (!p.isAlive()) break;
            }
        } catch (Exception e) {
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    public static List<String> runCommand(String cmd, String input) throws IOException, InterruptedException, ExecutionException {
        Process p = Runtime.getRuntime().exec(cmd);
        if (!StringUtils.isBlank(input)) {
            logger.debug("Input: \n" + input);
            OutputStream stdin = p.getOutputStream();
            stdin.write(input.getBytes());
            stdin.flush();
            stdin.close();
        }
        BufferedReader stdReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        List<String> result = new LinkedList<>();
        List<String> error = new LinkedList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future stdFuture = executor.submit(() -> readStream(stdReader, result, p));

        Future errFuture = executor.submit(() -> readStream(errReader, error, p));
        stdFuture.get();
        errFuture.get();
        executor.shutdown();
        executor.awaitTermination(-1, TimeUnit.MILLISECONDS);
        p.waitFor();
        stdReader.close();
        errReader.close();

        if (!error.isEmpty()) {
            logger.debug(String.format("[Error or Warning] of running %1$s\n%2$s", cmd, StringUtils.join(error, "\n")));
        }
        return result;
    }
}
