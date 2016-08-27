package gitp4.git.cmd;

import gitp4.CmdRunner;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitAdd {
    private static final String GIT_ADD_CMD = "git add %s";
    public static void run(final String files) throws Exception {
        if (files == null || files.isEmpty()) throw new NullPointerException("files");
        CmdRunner.run(() -> String.format(GIT_ADD_CMD, files), (cmdRes)-> "");
    }
}
