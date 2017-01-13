package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitRm {
    private static final String GIT_RM_CMD = Utils.getArgFormat("git rm -f %s");

    public static void run(final String files) {
        if (files == null || files.isEmpty()) throw new NullPointerException("files");
        CmdRunner.getGitCmdRunner().run(() -> Utils.convertToArgArray(String.format(GIT_RM_CMD, files)), (cmdRes) -> "");
    }
}
