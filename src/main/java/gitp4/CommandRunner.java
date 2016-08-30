package gitp4;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Created by chriskang on 8/23/2016.
 */
public class CommandRunner {
    private static final Logger logger = Logger.getLogger(CommandRunner.class);
    private static final long BUFFER_READ_INTERVAL = 10;

    public static List<String> runCommand(String cmd) throws IOException, InterruptedException, ExecutionException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        List<String> result = new LinkedList<>();
        //Read cmd output every BUFFER_READ_INTERVAL milliseconds to prevent the process hung due to the buffer full
        int i = 0;
        for (; !p.waitFor(BUFFER_READ_INTERVAL, TimeUnit.MILLISECONDS); ++i) {
            for (String res = reader.readLine(); res != null; res = reader.readLine()) {
                result.add(res);
            }
        }
        int j = 0;
        for (String res = reader.readLine(); res != null; res = reader.readLine(), ++j) {
            result.add(res);
        }
        logger.debug(String.format("read buffer (%1$d + %2$d) times", i, j));
        return result;
    }
}
