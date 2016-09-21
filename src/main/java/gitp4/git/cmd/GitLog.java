package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitLog {
    private static final String GIT_LOG_CMD = "git log %1$s %2$s";
    private static final String GIT_LOG_LATEST_COMMIT = "git log -1 --pretty=oneline";
    private static Set<Pattern> patterns = new HashSet<Pattern>() {{
        add(Pattern.compile("[^\\s\\.]+\\.\\.[^\\s\\.]+"));
        add(Pattern.compile("[^\\s\\.]+\\s+[^\\s\\.]+"));
    }};

    public static List<GitLogInfo> run(final String rangeInfo) {
        validateInput(rangeInfo);
        return CmdRunner.getGitCmdRunner().run(() -> String.format(GIT_LOG_CMD, GitLogInfo.CMD_PARAM, rangeInfo),
                (cmdRes) -> {
                    List<GitLogInfo> result = new LinkedList<>();
                    cmdRes.forEach(cur -> result.add(0, new GitLogInfo(cur)));
                    return result;
                });
    }

    public static List<GitFileInfo> getAllChangedFiles(final String rangeInfo) {
        validateInput(rangeInfo);
        return CmdRunner.getGitCmdRunner().run(() -> String.format(GIT_LOG_CMD, GitFileInfo.CMD_PARAM, rangeInfo),
                (cmdRes) -> {
                    List<GitFileInfo> result = new LinkedList<>();
                    cmdRes.forEach(cur -> result.add(0, new GitFileInfo(cur)));
                    return result;
                });
    }

    public static GitLogInfo getLatestCommit() {
        return CmdRunner.getGitCmdRunner().run(() -> GIT_LOG_LATEST_COMMIT,
                cmdRes -> {
                    if (cmdRes == null || cmdRes.size() != 1) {
                        throw new IllegalStateException(String.format("Error return of running git log -1: %s",
                                cmdRes == null ? "<NULL>" : StringUtils.join(cmdRes, "\n")));
                    }
                    return new GitLogInfo(cmdRes.get(0));
                });
    }

    private static void validateInput(String rangeInfo) {
        if (!StringUtils.isBlank(rangeInfo)) {
            boolean matches = patterns.stream().filter(cur -> cur.matcher(rangeInfo).matches()).findAny().isPresent();

            if (!matches) throw new IllegalArgumentException("rangeInfo: " + rangeInfo);
        }
    }
}
