package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 9/26/2016.
 */
public class GitBranch {
    private static final String[] BRANCH_CMD = new String[]{"git", "branch"};
    private static final String branchGroup = "branch";
    private static final Pattern branchPattern = Pattern.compile(String.format("\\*\\s+(?<%s>.+)", branchGroup));

    public static String getCurrentBranch() {
        return CmdRunner.getGitCmdRunner().run(() -> BRANCH_CMD, cmdRes -> {
            if (cmdRes == null || cmdRes.isEmpty()) {
                throw new IllegalStateException(String.format("Error response from %1$s:\n%2$s",
                        StringUtils.join(BRANCH_CMD, " "), cmdRes == null ? "<null>" : StringUtils.join(cmdRes, "\n")));
            }
            Optional<Matcher> optional = cmdRes.stream().map(branchPattern::matcher).filter(Matcher::matches).findAny();
            if (!optional.isPresent()) {
                throw new IllegalStateException(String.format("Error response from %1$s:\n%2$s",
                        StringUtils.join(BRANCH_CMD, " "), StringUtils.join(cmdRes, "\n")));
            }
            return optional.get().group(branchGroup);
        });
    }
}
