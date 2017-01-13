package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitCheckout {
    private static final String GIT_CHECKOUT_CMD = Utils.getArgFormat("git checkout %s");

    public static void run(final String parameters) {
        if (parameters == null || parameters.isEmpty()) throw new NullPointerException("parameters");
        CmdRunner.getGitCmdRunner().run(() -> Utils.convertToArgArray(String.format(GIT_CHECKOUT_CMD, parameters)), (cmdRes) -> "");
    }
}
