package gitp4;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by chriskang on 8/23/2016.
 */
public class CmdRunner {
    private static final Logger logger = Logger.getLogger(CmdRunner.class);

    private static final CmdRunner gitCmdRunner = new CmdRunner(CmdRunner::gitCmdOnError);
    private static final CmdRunner p4CmdRunner = new CmdRunner(CmdRunner::p4CmdOnError);
    private static final Set<String> gitErrorHeaders = new HashSet<>();
    private static final Set<String> p4ErrorHeaders = new HashSet<>();

    static {
        gitErrorHeaders.add("fatal:");
        gitErrorHeaders.add("error:");
        p4ErrorHeaders.add("Perforce password (P4PASSWD) invalid or unset.");
    }


    private final BiConsumer<String[], List<String>> onError;

    public CmdRunner(BiConsumer<String[], List<String>> onError) {
        this.onError = onError;
    }

    public <T> T run(Callable<String[]> getCmd, Function<List<String>, T> resultHandler) {
        return run(getCmd, resultHandler, "");
    }

    public <T> T run(Callable<String[]> getCmd, Function<List<String>, T> resultHandler, String input) {
        return Utils.runtimeExceptionWrapper(() -> {
            String[] cmd = getCmd.call();
            logger.debug("Running command: " + StringUtils.join(cmd, " "));
            List<String> cmdRes = CommandRunner.runCommand(cmd, input, null, onError);
            logger.debug("command output: \n" + StringUtils.join(cmdRes, "\n"));
            return resultHandler.apply(cmdRes);

        });
    }

    public static CmdRunner getGitCmdRunner() {
        return gitCmdRunner;
    }

    public static CmdRunner getP4CmdRunner() {
        return p4CmdRunner;
    }

    private static void gitCmdOnError(String[] cmd, List<String> error) {
        onError(cmd, error, gitErrorHeaders);
    }

    private static void p4CmdOnError(String[] cmd, List<String> error) {
        onError(cmd, error, p4ErrorHeaders);
    }

    private static void onError(String cmd[], List<String> error, Set<String> headers) {
        if (error != null && !error.isEmpty() && !StringUtils.isBlank(error.get(0))) {
            String msg = StringUtils.join(error, "\n");
            if (headers.stream().filter(error.get(0)::startsWith).findAny().isPresent()) {
                throw new GitP4Exception(String.format("%1$s\n%2$s", StringUtils.join(cmd, " "), msg));
            } else {
                logger.debug(msg);
            }
        }
    }
}
