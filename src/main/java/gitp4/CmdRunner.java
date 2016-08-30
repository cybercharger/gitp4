package gitp4;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by chriskang on 8/23/2016.
 */
public class CmdRunner {
    private static final Logger logger = Logger.getLogger(CmdRunner.class);
    public static <T> T run(Callable<String> getCmd, Function<List<String>, T> resultHandler) throws Exception {
        String cmd = getCmd.call();
        try {
            logger.debug("Running command: " + cmd);
            List<String> cmdRes = CommandRunner.runCommand(cmd);
            logger.debug("command output: \n" + StringUtils.join(cmdRes, "\n"));
            return resultHandler.apply(cmdRes);
        } catch (Exception e) {
            logger.error(String.format("Error running cmd %s", cmd), e);
            throw e;
        }
    }
}
