package gitp4.git;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitBranchInfo {
    private static final String masterBranchName = "master";
    private static final String branchGroupId = "branch";
    private static final Pattern pattern = Pattern.compile(String.format("\\*\\s+(?<%s>.+)", branchGroupId));

    private final String activeBranch;

    GitBranchInfo(List<String> cmdRes) {
        if (cmdRes == null || cmdRes.isEmpty()) throw new NullPointerException("cmdRes");
        Optional<Matcher> wanted = cmdRes.stream().map(pattern::matcher).filter(Matcher::matches).findFirst();
        if (!wanted.isPresent()) {
            throw new IllegalArgumentException("Cannot find active branch from " + StringUtils.join(cmdRes, "\n"));
        }
        activeBranch = wanted.get().group(branchGroupId);
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public boolean isMasterBranch() {
        return masterBranchName.equals(activeBranch);
    }
}
