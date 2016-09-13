package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitCommit {
    private static final String GIT_COMMIT_CMD = "git commit -m\"%s\"";
    private static final String GIT_COMMIT_F_CMD = "git commit -F %s";

    public static void run(final String comments) {
        if (StringUtils.isBlank(comments)) throw new NullPointerException("comments");
        CmdRunner.getGitCmdRunner().run(() -> String.format(GIT_COMMIT_CMD, comments), (cmdRes) -> "");
    }

    public static void commitFromFile(final String comments, final String changelist) {
        Utils.runtimeExceptionWrapper(() -> {
            if (StringUtils.isBlank(comments)) throw new NullPointerException("comments");
            Path tmpFile = Paths.get(changelist);
            Files.write(tmpFile, comments.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            CmdRunner.getGitCmdRunner().run(() -> String.format(GIT_COMMIT_F_CMD, Paths.get(changelist).toString()), (cmdRes) -> "");
            Files.deleteIfExists(tmpFile);
            return null;
        });

    }
}
