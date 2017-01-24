package gitp4;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;


/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    private static final Logger logger = Logger.getLogger(CommandRunner.class);
    private static final long BUFFER_READ_INTERVAL = 100;
    private static final String TMP_PREFIX = "commandRunner";
    private static final String TMP_POSTFIX = ".tmp";

    public static List<String> runCommand(String[] cmd,
                                          String input,
                                          String dir,
                                          BiConsumer<String[], List<String>> onError)
            throws IOException, InterruptedException, ExecutionException {
        if (cmd == null || cmd.length <= 0) throw new NullPointerException("cmd");
        File tmpFile = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (StringUtils.isNotBlank(dir) && Files.exists(Paths.get(dir))) {
                pb.directory(new File(dir));
                logger.debug("running command in dir: " + dir);
            } else {
                Path currentRelativePath = Paths.get("");
                logger.debug(currentRelativePath.toAbsolutePath().toString());
            }
            if (StringUtils.isNotBlank(input)) {
                tmpFile = File.createTempFile(TMP_PREFIX, TMP_POSTFIX);
                Files.write(Paths.get(tmpFile.getPath()), input.getBytes(), StandardOpenOption.CREATE);
                pb.redirectInput(tmpFile);
            }
            Process p = pb.start();

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
                if (onError != null) {
                    onError.accept(cmd, error);
                } else {
                    logger.debug(String.format("[Error or Warning] of running %1$s\n%2$s", StringUtils.join(cmd, " "), StringUtils.join(error, "\n")));
                }
            }
            return result;
        } finally {
            if (tmpFile != null) {
                tmpFile.deleteOnExit();
            }
        }
    }

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
}
