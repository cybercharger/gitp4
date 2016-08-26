package gitp4.git.cmd;

import gitp4.CmdRunner;
import gitp4.git.GitLogInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitLog {
    private static String GIT_LOG_CMD = "git log %1$s %1$s";
    private static Pattern pattern = Pattern.compile("[^\\s\\.]+\\.\\.[^\\s\\.]+");

    public static List<GitLogInfo> run(String rangeInfo) throws Exception {
        if (!StringUtils.isBlank(rangeInfo)) {
            Matcher matcher = pattern.matcher(rangeInfo);
            if (!matcher.matches()) throw new IllegalArgumentException("rangeInfo: " + rangeInfo);
        }

        return CmdRunner.run(() -> String.format(GIT_LOG_CMD, GitLogInfo.CMD_PARAM, rangeInfo),
                (cmdRes) -> {
                    List<GitLogInfo> result = new LinkedList<>();
                    cmdRes.forEach(cur -> result.add(0, new GitLogInfo(cur)));
                    return result;
                });
    }
}
