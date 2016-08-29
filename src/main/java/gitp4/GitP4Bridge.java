package gitp4;

import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import gitp4.git.cmd.*;
import gitp4.p4.*;
import gitp4.p4.cmd.P4Changes;
import gitp4.p4.cmd.P4Describe;
import gitp4.p4.cmd.P4Opened;
import gitp4.p4.cmd.P4Print;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/24/2016.
 */
class GitP4Bridge {
    private static Logger logger = Logger.getLogger(GitP4Bridge.class);

    private static final String commitCommentsTemplate = "%1$s [git-p4 depot-paths = %2$s: change = %3$s]";
    private static final String GIT_P4_SYNC_CMD_FMT = "%1$s...@%2$d,#head";
    private static final String lastSubmitTag = "last_p4_submit";
    private static final String p4IntBranchName = "p4-integ";

    private static final Path gitP4DirPath = Paths.get(".gitp4");
    private static final Path gitDirPath = Paths.get(".git");
    private static final Path gitP4ConfigFilePath = Paths.get(gitP4DirPath.toString(), "config");

    private static class MethodInfo {
        final int paramNum;
        final Method method;

        MethodInfo(int paramNum, Method method) {
            this.paramNum = paramNum;
            this.method = method;
        }
    }


    void operate(String[] args) throws InvocationTargetException, IllegalAccessException {
        Map<String, MethodInfo> methodMap = new HashMap<>();
        for (Method m : GitP4Bridge.class.getDeclaredMethods()) {
            GitP4Operation gpo = m.getAnnotation(GitP4Operation.class);
            if (gpo == null) continue;
            methodMap.put(m.getName(), new MethodInfo(gpo.paramNum(), m));
        }

        if (args.length < 1 || !methodMap.containsKey(args[0])) {
            logError(methodMap.keySet());
            return;
        }

        int requiredParamNum = methodMap.get(args[0]).paramNum;

        if (args.length < requiredParamNum + 1) {
            logError(methodMap.keySet());
            return;
        }
        List<String> newArgs = requiredParamNum > 0 ? Arrays.asList(Arrays.copyOfRange(args, 1, args.length)) : null;
        methodMap.get(args[0]).method.invoke(this, newArgs);
    }

    private static void logError(Set<String> operations) {
        logger.error("Please provide operation and proper parameters");
        logger.error(String.format("Valid operations are: \n%s", StringUtils.join(operations, "\n")));
    }

    @GitP4Operation(paramNum = 2)
    private void mock(List<String> args) {
        System.out.print(String.format("first param is: %1$s, second param is: %2$s", args.get(0), args.get(1)));
    }

    @GitP4Operation(paramNum = 1)
    private void clone(List<String> parameters) throws Exception {
        if (Files.exists(gitDirPath) || Files.exists(gitP4DirPath)) {
            throw new IllegalStateException("This folder is already initialized for git or cloned from p4 repo");
        }


        logger.info("Git init current directory...");
        GitInit.run("");

        List<P4ChangeInfo> p4Changes = P4Changes.run(parameters.get(0));
        if (p4Changes.isEmpty()) {
            logger.warn("There is nothing to clone from p4 repo, forgot to log in?");
            return;
        }
        logger.info(String.format("Totally %d changelist(s) to clone", p4Changes.size()));

        P4RepositoryInfo repoInfo = new P4RepositoryInfo(parameters.get(0));

        String lastChangelist = applyP4Changes(p4Changes, repoInfo);

        createGitP4Directory(repoInfo, lastChangelist);
        GitTag.run(lastSubmitTag, "git p4 clone");
        GitCheckout.run(String.format("-b %s", p4IntBranchName));
    }

    @GitP4Operation(paramNum = 0)
    private void sync(List<String> parameters) throws Exception {
        if (!Files.exists(gitP4ConfigFilePath)) {
            throw new IllegalStateException("Please run git p4 clone first");
        }
        Properties config = GitP4Config.load(gitP4ConfigFilePath);
        int lastChangelist = Integer.parseInt(config.getProperty(GitP4Config.lastSync)) + 1;
        P4RepositoryInfo repoInfo = new P4RepositoryInfo(config.getProperty(GitP4Config.p4Repo));

        List<P4ChangeInfo> p4Changes = P4Changes.run(String.format(GIT_P4_SYNC_CMD_FMT, repoInfo.getPath(), lastChangelist));
        if (p4Changes.isEmpty()) {
            logger.info("files are up to date");
            return;
        }
        applyP4Changes(p4Changes, repoInfo);
        config.setProperty(GitP4Config.lastSync, String.format("%d", lastChangelist));
        GitP4Config.save(config, gitP4ConfigFilePath, ".gitp4 config");
        updateGitP4Config();
    }

    @GitP4Operation(paramNum = 0)
    private void submit(List<String> parameters) throws Exception {
        GitLogInfo latest = GitLog.getLatestCommit();
        logger.info("Submit commits upto " + latest.getCommit());
        final String range = String.format("%1$s..%2$s", lastSubmitTag, latest.getCommit());
        List<GitLogInfo> logInfo = GitLog.run(range);
        if (logInfo.isEmpty()) {
            logger.warn("Nothing to submit to p4 repo");
            return;
        }
        logger.info(String.format("%d commit(s) in total to submit", logInfo.size()));
        logInfo.forEach(cur -> logger.info(String.format("%s: %s", cur.getCommit(), cur.getComment())));
        List<GitFileInfo> files = GitLog.getAllChangedFiles(range);
        Set<String> affectedFiles = new HashSet<>();
        for (GitFileInfo info : files) {
            switch (info.getChangeType()) {
                case Add:
                case Delete:
                case Modify:
                    affectedFiles.add(info.getOldFile());
                    break;
                case Rename:
                    affectedFiles.add(info.getOldFile());
                    affectedFiles.add(info.getOldFile());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown git operation type " + info.getChangeType());
            }
        }
        Properties config = GitP4Config.load(gitP4ConfigFilePath);
        P4RepositoryInfo p4Repo = new P4RepositoryInfo(config.getProperty(GitP4Config.p4Repo));

        logger.info("=== affected files ===");
        affectedFiles.stream().map(cur -> p4Repo.getPath() + cur).forEach(logger::info);

        List<P4FileOpenedInfo> p4Opened = P4Opened.run(p4Repo.getPathWithSubContents());

        if (p4Opened != null && !p4Opened.isEmpty()) {
            logger.info("=== p4 opened files ===");
            p4Opened.forEach(cur -> logger.info(cur.getFile()));
            Object[] intersection = p4Opened.stream().filter(cur -> {
                String file = cur.getFile().replace(p4Repo.getPath(), "");
                return affectedFiles.contains(file);
            }).toArray();
            if (intersection != null && intersection.length > 0)
                logger.warn(String.format("Conflict found, files also opened on p4 repo: \n%s", StringUtils.join(intersection, "\n")));
            return;
        }
    }

    private void gitAddRmChangelist(P4ChangeListInfo clInfo, P4RepositoryInfo p4Repository) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        logger.info(String.format("%d file(s) to clone/sync...", clInfo.getFiles().size()));
        Collection<Callable<Boolean>> callables = new LinkedList<>();
        for (P4FileInfo fileInfo : clInfo.getFiles()) {

            callables.add(() -> {
                String p4File = String.format("%1$s#%2$d", fileInfo.getFile(), fileInfo.getRevision());
                String outputFile = getGitFilePath(fileInfo.getFile(), p4Repository);
//                logger.info(String.format("%1$s -> %2$s", p4File, outputFile));
                boolean ret = true;
                if (P4Operation.delete == fileInfo.getOperation()) {
                    try {
                        GitRm.run(outputFile);
                    } catch (Exception e) {
                        logger.error("Failed to delete p4 file " + p4File, e);
                        ret = false;
                    }
                } else {
                    try {
                        P4Print.run(p4File, outputFile);
                        GitAdd.run(outputFile);
                    } catch (Exception e) {
                        logger.error("Failed to fetch p4 file " + p4File, e);
                        ret = false;
                    }
                }
                return ret;
            });

        }
        List<Future<Boolean>> futures = executor.invokeAll(callables);
        boolean alllGood = true;
        for (Future<Boolean> f : futures) {
             alllGood &= f.get();
        }
        executor.shutdown();
        executor.awaitTermination(-1, TimeUnit.DAYS);
        logger.info("Finished on changelist: " + clInfo.getChangelist());
        if (!alllGood) throw new IllegalStateException("Failed to clone/sync changelist " + clInfo.getChangelist());
    }

    private static String getGitFilePath(String p4FilePath, P4RepositoryInfo p4Repository) {
        return String.format("\"%s\"", p4FilePath.replace(p4Repository.getPath(), "./"));
    }

    private void gitCommitChangelist(P4ChangeListInfo clInfo, P4RepositoryInfo p4Repository) throws Exception {
        String comments = String.format(commitCommentsTemplate,
                clInfo.getFullComments(), p4Repository.getPath(), clInfo.getChangelist());
        GitCommit.run(comments);
    }

    private String applyP4Changes(List<P4ChangeInfo> p4Changes, P4RepositoryInfo repoInfo) throws Exception {
        String lastChangelist = "0";
        int i = 1;
        int total = p4Changes.size();
        for (P4ChangeInfo change : p4Changes) {
            logger.info(String.format("[%1$d/%2$d]: %3$s", i++, total, change.toString()));
            P4ChangeListInfo clInfo = P4Describe.run(change.getChangeList());
            gitAddRmChangelist(clInfo, repoInfo);
            gitCommitChangelist(clInfo, repoInfo);
            lastChangelist = clInfo.getChangelist();
        }
        return lastChangelist;
    }

    private void createGitP4Directory(P4RepositoryInfo p4RepositoryInfo, String lastChangelist) throws Exception {
        if (Files.exists(gitP4DirPath)) {
            throw new IllegalStateException("This folder is already initialized for git or cloned from p4 repo");
        }
        Files.createDirectory(gitP4DirPath);
        Properties config = new Properties();
        config.setProperty(GitP4Config.p4Repo, p4RepositoryInfo.getPath());
        config.setProperty(GitP4Config.lastSync, lastChangelist);
        GitP4Config.save(config, gitP4ConfigFilePath, ".gitp4 config");
        updateGitP4Config();
    }

    private void updateGitP4Config() throws Exception {
        GitAdd.run(gitP4ConfigFilePath.toString());
        GitCommit.run("commit .gitp4");
    }
}
