package gitp4;

import gitp4.git.cmd.GitAdd;
import gitp4.git.cmd.GitCommit;
import gitp4.git.cmd.GitInit;
import gitp4.git.cmd.GitRm;
import gitp4.p4.*;
import gitp4.p4.cmd.P4Changes;
import gitp4.p4.cmd.P4Describe;
import gitp4.p4.cmd.P4Print;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitP4Bridge {
    private static Logger logger = Logger.getLogger(GitP4Bridge.class);

    private static final String commitCommentsTemplate = "%1$s\n [git-p4 depot-paths = %2$s: change = %3$s]";

    public void clone(String p4Repository) throws Exception {
        if (StringUtils.isBlank(p4Repository)) throw new NullPointerException("p4Repository");
        logger.info("Git init current directory...");
        GitInit.run("");

        List<P4Change> p4Changes = P4Changes.run(p4Repository, null, null);
        p4Changes.sort(P4Change::compareTo);
        logger.info(String.format("Totally %d changelist(s) to clone", p4Changes.size()));

        P4RepositoryInfo repoInfo = new P4RepositoryInfo(p4Repository);

        for (P4Change change : p4Changes) {
            P4ChangeListInfo clInfo = P4Describe.run(change);
            gitAddRmChangelist(clInfo, repoInfo);
            gitCommitChangelist(clInfo, repoInfo);
        }
    }

    public void sync(String p4Repository, String toChangelist, String fromChangelist) {

    }

    private void gitAddRmChangelist(P4ChangeListInfo clInfo, P4RepositoryInfo p4Repository) throws Exception {
        List<String> addFiles = new LinkedList<>();
        List<String> deleteFiles = new LinkedList<>();
        for (P4FileInfo fileInfo : clInfo.getFiles()) {
            String p4File = String.format("%1$s#%2$d", fileInfo.getFile(), fileInfo.getRevision());
            String outputFile = getGitFilePath(fileInfo.getFile(), p4Repository);
            if (P4Operation.delete == fileInfo.getOperation()) {
                deleteFiles.add(outputFile);
            } else {
                P4Print.run(p4File, outputFile);
                addFiles.add(outputFile);
            }
        }
        if (!addFiles.isEmpty()) GitAdd.run(StringUtils.join(addFiles, " "));
        if (!deleteFiles.isEmpty()) GitRm.run(StringUtils.join(deleteFiles, " "));
    }

    private static String getGitFilePath(String p4FilePath, P4RepositoryInfo p4Repository) {
        return String.format("\"%s\"", p4FilePath.replace(p4Repository.getPath(), "./"));
    }

    private void gitCommitChangelist(P4ChangeListInfo clInfo, P4RepositoryInfo p4Repository) throws Exception {
        String comments = String.format(commitCommentsTemplate,
                clInfo.getFullComments(), p4Repository.getPath(), clInfo.getChangelist());
        GitCommit.run(comments);
    }
}
