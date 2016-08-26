package gitp4.git.cmd;

import gitp4.CmdRunner;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitRm {
    private static final String GIT_RM_CMD = "git rm %s";
    public static void run(String files) throws Exception {
        if (files == null || files.isEmpty()) throw new NullPointerException("files");
        CmdRunner.run(()->String.format(GIT_RM_CMD, files), (cmdRes)->"");
    }
}