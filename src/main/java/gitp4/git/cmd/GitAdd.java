package gitp4.git.cmd;

import java.util.List;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitAdd {
    public static void run(final List<String> files) {
        GitAddRm.run(GitAddRm.Operation.Add, files);
    }

    public static void singleFile(final String file) {
        GitAddRm.run(GitAddRm.Operation.Add, file);
    }
}
