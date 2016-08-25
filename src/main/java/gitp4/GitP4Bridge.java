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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by chriskang on 8/24/2016.
 */
public class GitP4Bridge {
    private static Logger logger = Logger.getLogger(GitP4Bridge.class);

    private static final String commitCommentsTemplate = "%1$s [git-p4 depot-paths = %2$s: change = %3$s]";

    private static final Path gitP4DirPath = Paths.get(".gitp4");
    private static final Path gitDirPath = Paths.get(".git");
    private static final Path gitP4ConfigFilePath = Paths.get(gitP4DirPath.toString(), "config");

    @GitP4Operation
    public void clone(String parameters) throws Exception {

        if (StringUtils.isBlank(parameters)) throw new NullPointerException("parameters");

        if (Files.exists(gitDirPath) || Files.exists(gitP4DirPath)) {
            throw new IllegalStateException("This folder is already initialized for git or cloned from p4 repo");
        }


        logger.info("Git init current directory...");
        GitInit.run("");

        List<P4ChangeInfo> p4Changes = P4Changes.run(parameters);
        p4Changes.sort(P4ChangeInfo::compareTo);
        logger.info(String.format("Totally %d changelist(s) to clone", p4Changes.size()));

        P4RepositoryInfo repoInfo = new P4RepositoryInfo(parameters);

        String lastChangelist = "0";
        for (P4ChangeInfo change : p4Changes) {
            P4ChangeListInfo clInfo = P4Describe.run(change.getChangeList());
            gitAddRmChangelist(clInfo, repoInfo);
            gitCommitChangelist(clInfo, repoInfo);
            lastChangelist = clInfo.getChangelist();
        }

        createGitP4Directory(repoInfo, lastChangelist);
    }

    @GitP4Operation
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

    private void createGitP4Directory(P4RepositoryInfo p4RepositoryInfo, String lastChangelist) throws IOException {
        if (Files.exists(gitP4DirPath)) {
            throw new IllegalStateException("This folder is already initialized for git or cloned from p4 repo");
        }
        Files.createDirectory(gitP4DirPath);
        Properties config = new Properties();
        config.setProperty(GitP4Config.p4Repo, p4RepositoryInfo.getPath());
        config.setProperty(GitP4Config.lastSync, lastChangelist);
        GitP4Config.save(config, gitP4ConfigFilePath, ".gitp4 config");
    }
}
