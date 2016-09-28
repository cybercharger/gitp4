package gitp4;

import gitp4.cli.*;
import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import gitp4.git.cmd.*;
import gitp4.p4.*;
import gitp4.p4.cmd.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 8/24/2016.
 */
class GitP4Bridge {
    private static Logger logger = Logger.getLogger(GitP4Bridge.class);
    private static final Set<Path> emptyCheckIgnore = new HashSet<Path>() {{
        add(Paths.get(""));
        add(Paths.get("gitp4.log"));
        add(Paths.get("gitp4_caution.log"));
    }};

    private static class MethodInfo {
        final Class<? extends GitP4OperationOption> optionClass;
        final Method method;
        final String description;

        MethodInfo(Class<? extends GitP4OperationOption> option, Method method, String description) {
            this.optionClass = option;
            this.method = method;
            this.description = description;
        }
    }


    void operate(String[] args) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Map<String, MethodInfo> methodMap = new HashMap<>();
        for (Method m : GitP4Bridge.class.getDeclaredMethods()) {
            GitP4Operation gpo = m.getAnnotation(GitP4Operation.class);
            if (gpo == null) continue;
            String operationName = StringUtils.isBlank(gpo.operationName()) ? m.getName() : gpo.operationName();
            methodMap.put(operationName, new MethodInfo(gpo.option(), m, gpo.description()));
        }

        if (args.length < 1 || !methodMap.containsKey(args[0])) {
            logError(methodMap);
            return;
        }

        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            GitP4OperationOption option = methodMap.get(args[0]).optionClass.getDeclaredConstructor(String[].class).newInstance(new Object[]{newArgs});
            if (!option.parse()) return;
            methodMap.get(args[0]).method.invoke(this, option);
        } catch (Exception e) {
            if (!logException(e)) {
                logger.error("Unknown error occurred: ", e);
            }
        }
    }

    private boolean logException(Exception e) {
        Throwable exp = e;
        if (e instanceof InvocationTargetException) {
            exp = e.getCause() != null ? e.getCause() : e;
            if (exp instanceof GitP4Exception) {
                logger.error("Error occurred: " + exp.getMessage());
                logger.debug("Error details: ", e);
            } else {
                logger.error("Error occurred: ", exp);
            }
        }
        return true;
    }

    private static void logError(Map<String, MethodInfo> methodInfoMap) {
        logger.error("Please provide operation and proper parameters");
        StringBuilder sb = new StringBuilder();
        methodInfoMap.entrySet().forEach(cur -> sb.append(String.format("%1$s:\t%2$s\n", cur.getKey(), cur.getValue().description)));
        logger.error(String.format("Valid operations are: \n%s\nPlease type <operation> --help for details", sb.toString()));
    }

    @GitP4Operation(option = MockOption.class, description = "mock operation")
    private void mock(GitP4OperationOption input) {
        if (input == null) throw new NullPointerException("input");

        MockOption option = MockOption.class.cast(input);
        System.out.print(String.format("mock:\t\t%1$s\nprofile:\t%2$s\nMaxThreads:\t%3$s\nPageSize:\t%4$s\nP4SyncDelay:\t%5$sms\n",
                option.getMock(),
                option.getProfile(),
                option.getMaxThreads(),
                option.getPageSize(),
                option.getP4SyncDelay()));
    }

    private String init(CloneOption option) throws Exception {
        if (option == null) throw new NullPointerException("option");
        Profile profile = new Profile(option.getProfile(), false);

        if (!option.bypassEmptyCheck()) {
            Path curDir = Paths.get("");
            Files.walk(curDir).forEach(cur -> {
                if (!emptyCheckIgnore.contains(cur)) {
                    throw new GitP4Exception(String.format("%1$s is not empty: %2$s", curDir.toAbsolutePath().normalize(), cur));
                }
            });
        } else {
            logger.warn("skip dir empty check, existing content may be destroyed");
        }

        if (Files.exists(Profile.gitDirPath) || Files.exists(Profile.gitP4DirPath)) {
            throw new GitP4Exception("This folder is already initialized for git or cloned from p4 repo");
        }

        P4RepositoryInfo repoInfo = new P4RepositoryInfo(option.getCloneString());
        logger.info("p4 root is " + repoInfo.getPathWithSubContents());

        List<P4FileOpenedInfo> p4Opened = P4Opened.run(repoInfo.getPathWithSubContents());
        if (p4Opened != null && !p4Opened.isEmpty()) {
            String[] files = p4Opened.stream().map(P4FileOpenedInfo::getFile).toArray(String[]::new);
            String msg = String.format("Please submit or revert the following p4 opened files and try again.\n%s",
                    StringUtils.join(files, "\n"));
            throw new GitP4Exception(msg);
        }


        if (Files.exists(Profile.gitP4DirPath)) {
            throw new GitP4Exception("This folder is already initialized for git or cloned from p4 repo");
        }
        Files.createDirectory(Profile.gitP4DirPath);
        Properties config = new Properties();
        config.setProperty(GitP4Config.p4Repo, repoInfo.getPathWithSubContents());
        config.setProperty(GitP4Config.viewMap, option.getViewString());
        config.setProperty(GitP4Config.lastSync, "-1");
        config.setProperty(GitP4Config.submitIgnore, profile.getConfigFilePath().toString());
        config.setProperty(GitP4Config.syncBranch, option.getP4IntBranchName());
        config.setProperty(GitP4Config.submitBranch, option.getSubmitBranchName());
        GitP4Config.save(config, profile.getConfigFilePath(), ".gitp4 config");


        logger.info("Git init current directory...");
        GitInit.run("");
        updateGitP4Config(profile.getConfigFilePath().toString());

        String[] map = option.getViewMap().stream()
                .map(cur -> String.format("%1$s%2$s/...", repoInfo.getPath(), cur))
                .toArray(String[]::new);
        if (map != null && map.length > 0) logger.info("initialized for:\n" + StringUtils.join(map, "\n"));
        return option.getCloneString();
    }

    @GitP4Operation(option = CloneOption.class, description = "clone a specific p4 repository")
    private void clone(CloneOption option) throws Exception {
        String cloneString = init(option);
        Profile profile = new Profile(option.getProfile(), true);
        logger.info("cloning " + cloneString);
        List<P4ChangeInfo> p4Changes = P4Changes.run(cloneString);
        if (p4Changes.isEmpty()) {
            logger.warn("There is nothing to clone from p4 repo");
            return;
        }

        logger.info(String.format("Totally %d changelist(s) to clone", p4Changes.size()));

        applyP4Changes(p4Changes, new P4RepositoryInfo(cloneString), option, profile);

        GitAdd.run(profile.getConfigFilePath().toString());
        GitCommit.run("update git p4 config");

        GitTag.tag(profile.getLastSubmitTag(), "git p4 clone");
        GitCheckout.run(String.format("-b %s", option.getP4IntBranchName()));
    }

    @GitP4Operation(option = EmptyOption.class, description = "sync code from the specified p4 repository")
    private void sync(EmptyOption option) throws Exception {
        Profile profile = new Profile(option.getProfile(), true);
        checkWorkingDir(profile.getConfigFilePath());

        Properties config = GitP4Config.load(profile.getConfigFilePath());
        String expectedBranch = config.getProperty(GitP4Config.syncBranch);
        if (expectedBranch == null) {
            throw new GitP4Exception(String.format("Please set %1$s in your %2$s", GitP4Config.syncBranch, profile.getConfigFilePath()));
        }
        String actualBranch = GitBranch.getCurrentBranch();
        if (!expectedBranch.equals(actualBranch)) {
            throw new GitP4Exception(String.format("Please switch to branch [%s] and then run this command again", expectedBranch));
        }

        int lastChangelist = Integer.parseInt(config.getProperty(GitP4Config.lastSync)) + 1;
        P4RepositoryInfo repoInfo = new P4RepositoryInfo(config.getProperty(GitP4Config.p4Repo));

        List<P4ChangeInfo> p4Changes = P4Changes.run(String.format(Profile.GIT_P4_SYNC_CMD_FMT, repoInfo.getPath(), lastChangelist));
        if (p4Changes.isEmpty()) {
            logger.info("files are up to date");
            return;
        }

        List<P4FileOpenedInfo> p4Opened = P4Opened.run(repoInfo.getPathWithSubContents());
        if (p4Opened != null && !p4Opened.isEmpty()) {
            String[] files = p4Opened.stream().map(P4FileOpenedInfo::getFile).toArray(String[]::new);
            logger.error(String.format("Please submit or revert the following p4 opened files and try again.\n%s",
                    StringUtils.join(files, "\n")));
            return;
        }

        logger.info(String.format("Totally %d changelist(s) to sync", p4Changes.size()));

        applyP4Changes(p4Changes, repoInfo, option, profile);
        updateGitP4Config(profile.getConfigFilePath().toString());
    }

    @GitP4Operation(option = P4clOperation.class, operationName = "p4cl", description = "create a p4 changelist for git changes")
    private void createP4Changelist(P4clOperation option) throws Exception {
        Profile profile = new Profile(option.getProfile(), true);
        checkWorkingDir(profile.getConfigFilePath());
        Properties config = GitP4Config.load(profile.getConfigFilePath());
        String expectedBranch = config.getProperty(GitP4Config.submitBranch);
        if (expectedBranch == null) {
            throw new GitP4Exception(String.format("Please set %1$s in your %2$s", GitP4Config.submitBranch, profile.getConfigFilePath()));
        }
        String actualBranch = GitBranch.getCurrentBranch();
        if (!expectedBranch.equals(actualBranch)) {
            throw new GitP4Exception(String.format("Please switch to branch [%s] and then run this command again", expectedBranch));
        }

        GitLogInfo latest = GitLog.getLatestCommit();
        logger.info("Commits submitted upto " + latest.getCommit());
        logger.info("Checking tag: " + profile.getLastSubmitTag());
        final String range = (GitTag.tagExisting(profile.getLastSubmitTag())) ?
                String.format("%1$s..%2$s", profile.getLastSubmitTag(), latest.getCommit()) :
                String.format("%1$s %2$s", GitRevList.getFirstCommit(), latest.getCommit());

        List<GitLogInfo> logInfo = GitLog.run(range);
        if (logInfo.isEmpty()) {
            logger.warn("Nothing to submit to p4 repo");
            return;
        }
        logger.info(String.format("%d commit(s) in total to submit", logInfo.size()));
        logInfo.forEach(cur -> logger.info(String.format("%1$s: %2$s", cur.getCommit(), cur.getComment())));
        List<GitFileInfo> files = GitLog.getAllChangedFiles(range);
        Set<String> affectedFiles = new HashSet<>();
        Set<String> ignoredFiles = new HashSet<>();
        Set<String> rawViews = GitP4Config.getViews(profile.getConfigFilePath()).stream().map(cur -> Paths.get(cur).toString()).collect(Collectors.toSet());
        Set<String> ignorePattern = GitP4Config.getSubmitIgnore(profile.getConfigFilePath()).stream().map(cur -> Paths.get(cur).toString()).collect(Collectors.toSet());
        logger.info(String.format("IGNORE PATTERN:\n%s", StringUtils.join(ignorePattern, "\n")));

        for (GitFileInfo info : files) {
            String file = info.getFile();
            if (Utils.collectionContains(ignorePattern, Paths.get(file).toString()::startsWith)) {
                ignoredFiles.add(file);
                continue;
            }
            if (rawViews.isEmpty() || Utils.collectionContains(rawViews, Paths.get(file).toString()::startsWith)) {
                affectedFiles.add(file);
            } else {
                throw new GitP4Exception(String.format("%s is not under the p4 view map, please update your .gitp4/config", file));
            }
        }

        logger.info(String.format("IGNORED:\n%s", StringUtils.join(ignoredFiles, "\n")));

        final Set<String> finalCopy = affectedFiles;
        P4RepositoryInfo p4Repo = new P4RepositoryInfo(config.getProperty(GitP4Config.p4Repo) + P4RepositoryInfo.TRIPLE_DOTS);
        List<P4FileOpenedInfo> p4Opened = P4Opened.run(p4Repo.getPathWithSubContents());

        if (p4Opened != null && !p4Opened.isEmpty()) {
            String[] p4OpenedFiles = p4Opened.stream().map(P4FileOpenedInfo::getFile).toArray(String[]::new);
            logger.info(String.format("p4 opened files:\n%s", StringUtils.join(p4OpenedFiles, "\n")));
            Object[] intersection = p4Opened.stream().filter(cur -> {
                String file = cur.getFile().replace(p4Repo.getPath(), "");
                return finalCopy.contains(file);
            }).toArray();
            if (intersection != null && intersection.length > 0) {
                throw new GitP4Exception(String.format("Conflict found, files also opened on p4 repo: \n%s", StringUtils.join(intersection, "\n")));
            }
        }

        Map<String, String> gitP4DepotFileMap = affectedFiles.stream()
                .collect(Collectors.toMap(cur -> cur, cur -> p4Repo.getPath() + cur));
        logger.info(String.format("%1$d affected file(s):\n%2$s", affectedFiles.size(), StringUtils.join(affectedFiles, "\n")));
        Map<String, String> p4ExistingFiles = new HashMap<>();

        logger.info("checking p4 files...");
        int lastSync = Integer.parseInt(config.getProperty(GitP4Config.lastSync));
        Set<String> outOfDate = new HashSet<>();
        P4FileStatInfo fileStatInfo = P4Fstat.batchGetFileStats(gitP4DepotFileMap.values());
        fileStatInfo.getFiles().forEach(info -> {
            if (!P4Operation.delete.equals(info.getOperation())) {
                p4ExistingFiles.put(info.getDepotFile(), info.getClientFile());
            }
            if (info.getLastChangelist() > lastSync) {
                outOfDate.add(info.getDepotFile());
            }
        });

        if (!outOfDate.isEmpty()) {
            String msg = String.format("Files are changed after changelist %1$d:\n%2$s", lastSync, StringUtils.join(outOfDate, "\n"));
            if (!option.isForced()) {
                throw new GitP4Exception(msg);
            } else {
                logger.warn(msg);
            }
        }

        P4Sync.syncToLatest(p4Repo);

        Map<String, String> copyMap = new HashMap<>();
        Set<String> editSet = new HashSet<>();
        Set<String> addSet = new HashSet<>();
        Set<String> deleteSet = new HashSet<>();
        for (Map.Entry<String, String> cur : gitP4DepotFileMap.entrySet()) {

            Path source = Paths.get(cur.getKey());
            String p4File = cur.getValue();
            boolean gitExists = Utils.fileExists(source.toString());
            if (!gitExists) {
                logger.warn(String.format("git file %s doesn't exist, please double check it's deleted", source));
            }

            boolean p4Exists = p4ExistingFiles.containsKey(p4File);
            if (gitExists) {
                String target;
                if (p4Exists) {
                    editSet.add(p4File);
                    target = p4ExistingFiles.get(p4File);
                } else {
                    target = Paths.get(p4Repo.getPathMap().getLocalPath() + cur.getKey()).toString();
                    addSet.add(target);
                }
                copyMap.put(source.toString(), target);

            } else {
                if (p4Exists) {
                    deleteSet.add(p4File);
                } else {
                    logger.warn(String.format("ignore file %s", cur.getKey()));
                }
            }
        }

        if (option.dryrun()) {
            logger.info("\nDry-run result:");
            logger.info(String.format("\np4 edit:\n%s", StringUtils.join(editSet, "\n")));
            logger.info(String.format("\np4 delete:\n%s", StringUtils.join(deleteSet, "\n")));
            logger.info(String.format("\np4 add:\n%s", StringUtils.join(addSet, "\n")));
            logger.info(String.format("\ncopy map\n%s", StringUtils.join(copyMap.entrySet(), "\n")));
            logger.info("\n");
            return;
        }

        String p4cl = P4Change.createEmptyChangeList(option.getMessage());

        logger.info("p4 edit...");
        P4Edit.batch(editSet, p4cl);

        logger.info("p4 delete..");
        P4Delete.batch(deleteSet, p4cl);

        List<Callable<Boolean>> theCallable = new LinkedList<>();
        for (Map.Entry<String, String> entry : copyMap.entrySet()) {

            Files.createDirectories(Paths.get(entry.getValue()).getParent());

            theCallable.add(() -> {
                try {
                    Files.write(Paths.get(entry.getValue()),
                            Files.readAllBytes(Paths.get(entry.getKey())),
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    return true;
                } catch (Exception e) {
                    logger.error(String.format("Failed to copy file %1$s -> %2$s", entry.getKey(), entry.getValue()), e);
                    return false;
                }
            });
        }

        // copying file in parallel
        logger.info("copying files...");
        if (!theCallable.isEmpty()) {
            if (!Utils.runConcurrentlyAndAggregate(option.getMaxThreads(), theCallable)) {
                throw new RuntimeException("Error occurred when copying files");
            }
        }

        logger.info("p4 add...");
        P4Add.batch(addSet, p4cl);

        fileStatInfo = P4Fstat.batchGetFileStats(gitP4DepotFileMap.values());
        outOfDate.clear();
        fileStatInfo.getFiles().forEach(info -> {
            if (info.getLastChangelist() > lastSync) {
                outOfDate.add(info.getDepotFile());
            }
        });
        if (outOfDate.isEmpty()) {
            logger.info(String.format(Profile.submitHints, p4cl, latest.getCommit()));
        } else {
            logger.warn(String.format("Please double check the following files which are changed after %1$d:\n%2$s",
                    lastSync, StringUtils.join(outOfDate, "\n")));
        }
    }

    private static void pagedActionOnFiles(Collection<String> files, Consumer<String> action, String log, int pageSize) throws Exception {
        if (!files.isEmpty()) {
            logger.info(log);
            List<String> filesWithQuotes = files.stream()
                    .map(cur -> String.format("\"%s\"", cur))
                    .collect(Collectors.toList());
            Utils.pagedAction(filesWithQuotes, pageSize, page -> action.accept(StringUtils.join(page, " ")));
        }
    }

    private void applyP4Changes(List<P4ChangeInfo> p4Changes,
                                P4RepositoryInfo repoInfo,
                                GitP4OperationOption option,
                                Profile profile) throws Exception {
        int i = 1;
        int total = p4Changes.size();

        Set<String> views = GitP4Config.getViewsWithRoot(profile.getConfigFilePath());
        if (!views.isEmpty()) {
            logger.info("only files under following directories will be cloned:\n" + StringUtils.join(views, "\n"));
        }

        for (P4ChangeInfo change : p4Changes) {
            logger.info(String.format("[%1$d/%2$d]: %3$s", i++, total, change.toString()));

            copySingleChangelistAndGitCommit(change, repoInfo, views, option, profile);
        }
    }

    private void copySingleChangelistAndGitCommit(P4ChangeInfo p4Change,
                                                  P4RepositoryInfo repoInfo,
                                                  Set<String> views,
                                                  GitP4OperationOption option,
                                                  Profile profile) throws Exception {
        P4FileStatInfo info = P4Fstat.getChangelistStats(p4Change.getChangeList(), repoInfo);
        logger.info(String.format("%d affected file(s)", info.getFiles().size()));
        List<String> addFiles = new LinkedList<>();
        List<String> removeFiles = new LinkedList<>();
        List<String> ignoredFiles = new LinkedList<>();
        List<Callable<Boolean>> theCallable = new LinkedList<>();
        for (P4FileInfoEx file : info.getFiles()) {
            if (!views.isEmpty() && !views.stream().filter(file.getDepotFile()::startsWith).findAny().isPresent()) {
                ignoredFiles.add(file.getDepotFile());
                continue;
            }
            final String target = file.getDepotFile().replace(repoInfo.getPath(), "");
            if (P4Operation.delete == file.getOperation()) {
                if (Utils.fileExists(target)) {
                    removeFiles.add(target);
                } else {
                    logger.warn(String.format("ignore deleting of nonexistent file %1$s from cl %2$s", target, p4Change.getChangeList()));
                }
                continue;
            }

            addFiles.add(target);
            theCallable.add(() -> {
                try {
                    Files.write(Paths.get(target),
                            Files.readAllBytes(Paths.get(file.getClientFile())),
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    return true;
                } catch (Exception e) {
                    logger.error(String.format("Failed to copy file %1$s -> %2$s", file.getClientFile(), target), e);
                    return false;
                }
            });
        }
        if (!ignoredFiles.isEmpty()) {
            logger.info(String.format("%d file(s) is/are ignored", ignoredFiles.size()));
            logger.debug("ignored files are:\n" + StringUtils.join(ignoredFiles, "\n"));
        }
        if (!addFiles.isEmpty()) {
            logger.info("syncing p4 client to change list " + p4Change.getChangeList());
            P4Sync.forceSyncTo(repoInfo, p4Change.getChangeList());
            Thread.sleep(option.getP4SyncDelay());
        }

        // git rm files first, to avoid the case that move/delete & move/add in the same changelist, to rename a specific
        // file name. This happens when renaming files in IntelliJ
        pagedActionOnFiles(removeFiles, GitRm::run, "Git rm...", option.getPageSize());

        for (String target : addFiles) {
            Files.createDirectories(Paths.get(target).toAbsolutePath().getParent());
        }


        // copying file in parallel
        if (!theCallable.isEmpty()) {
            logger.info(String.format("%d file(s) in total to copy...", theCallable.size()));
            if (!Utils.runConcurrentlyAndAggregate(option.getMaxThreads(), theCallable)) {
                throw new RuntimeException("Error occurred when copying files for changelist " + p4Change.getChangeList());
            }
        }

        pagedActionOnFiles(addFiles, GitAdd::run, "Git add...", option.getPageSize());

        updateLastSyncAndGitAdd(p4Change.getChangeList(), profile.getConfigFilePath());
        String comments = String.format(Profile.commitCommentsTemplate,
                info.getDescription(),
                repoInfo.getPath(),
                p4Change.getChangeList(),
                p4Change.getP4UserInfo().toString(),
                p4Change.getDate());

        GitCommit.commitFromFile(comments, p4Change.getChangeList());
    }

    private void updateGitP4Config(String path) throws Exception {
        GitAdd.run(path);
        GitCommit.run("commit for .gitp4");
    }

    private void updateLastSyncAndGitAdd(String lastSync, Path path) throws Exception {
        Properties config = GitP4Config.load(path);
        config.setProperty(GitP4Config.lastSync, lastSync);
        GitP4Config.save(config, path, "");
        GitAdd.run(path.toString());
    }

    private void checkWorkingDir(Path path) {
        try {
            GitP4Config.load(path);
        } catch (IOException e) {
            throw new GitP4Exception("Please run this command under the directory where you ran clone");
        }
    }
}
