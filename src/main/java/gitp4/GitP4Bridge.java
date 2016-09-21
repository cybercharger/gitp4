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

    private static final String commitCommentsTemplate = "p4-%3$s: %1$s\n[p4 depot = %2$s change = %3$s from %4$s on %5$s]";
    private static final String submitHints = "p4 changelist %1$s has been created. Please re-tag last_p4_submit to %2$s after having it checked in";

    private static final String GIT_P4_SYNC_CMD_FMT = "%1$s...@%2$d,#head";
    private static final String lastSubmitTag = "last_p4_submit";
    private static final String p4IntBranchName = "p4-integ";

    private static final Path gitP4DirPath = Paths.get(".gitp4");
    private static final Path gitDirPath = Paths.get(".git");
    private static final Path gitP4ConfigFilePath = Paths.get(gitP4DirPath.toString(), "config");
    private static final Set<Path> emptyCheckIgnore = new HashSet<Path>() {{
        add(Paths.get(""));
        add(Paths.get("gitp4.log"));
        add(Paths.get("gitp4_caution.log"));
    }};

    //TODO: move this to properties
    private static final int MAX_THREADS = 10;
    private static final int GIT_ADD_RM_PAGE_SIZE = 20;
    // delay in milliseconds after p4 sync -f @=
    private static final int P4_SYNC_DELAY = 50;

    private static class MethodInfo {
        final Class<? extends GitP4OperationOption> optionClass;
        final Method method;

        MethodInfo(Class<? extends GitP4OperationOption> option, Method method) {
            this.optionClass = option;
            this.method = method;
        }
    }


    void operate(String[] args) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Map<String, MethodInfo> methodMap = new HashMap<>();
        for (Method m : GitP4Bridge.class.getDeclaredMethods()) {
            GitP4Operation gpo = m.getAnnotation(GitP4Operation.class);
            if (gpo == null) continue;
            methodMap.put(m.getName(), new MethodInfo(gpo.option(), m));
        }

        if (args.length < 1 || !methodMap.containsKey(args[0])) {
            logError(methodMap.keySet());
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

    private static void logError(Set<String> operations) {
        logger.error("Please provide operation and proper parameters");
        logger.error(String.format("Valid operations are: \n%s\nPlease type <operation> --help for details", StringUtils.join(operations, "\n")));
    }

    @GitP4Operation(option = MockOption.class)
    private void mock(GitP4OperationOption input) {
        if (input == null) throw new NullPointerException("input");

        MockOption option = MockOption.class.cast(input);

        System.out.print(option.getMock());
    }

    private String init(CloneOption option) throws Exception {
        if (option == null) throw new NullPointerException("option");

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

        if (Files.exists(gitDirPath) || Files.exists(gitP4DirPath)) {
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


        if (Files.exists(gitP4DirPath)) {
            throw new GitP4Exception("This folder is already initialized for git or cloned from p4 repo");
        }
        Files.createDirectory(gitP4DirPath);
        Properties config = new Properties();
        config.setProperty(GitP4Config.p4Repo, repoInfo.getPathWithSubContents());
        config.setProperty(GitP4Config.viewMap, option.getViewString());
        config.setProperty(GitP4Config.lastSync, "-1");
        config.setProperty(GitP4Config.submitIgnore, gitP4ConfigFilePath.toString());
        GitP4Config.save(config, gitP4ConfigFilePath, ".gitp4 config");


        logger.info("Git init current directory...");
        GitInit.run("");
        updateGitP4Config();

        String[] map = option.getViewMap().stream()
                .map(cur -> String.format("%1$s%2$s/...", repoInfo.getPath(), cur))
                .toArray(String[]::new);
        if (map != null && map.length > 0) logger.info("initialized for:\n" + StringUtils.join(map, "\n"));
        return option.getCloneString();
    }

    @GitP4Operation(option = CloneOption.class)
    private void clone(CloneOption option) throws Exception {
        String cloneString = init(option);
        logger.info("cloning " + cloneString);
        List<P4ChangeInfo> p4Changes = P4Changes.run(cloneString);
        if (p4Changes.isEmpty()) {
            logger.warn("There is nothing to clone from p4 repo");
            return;
        }

        logger.info(String.format("Totally %d changelist(s) to clone", p4Changes.size()));

        applyP4Changes(p4Changes, new P4RepositoryInfo(cloneString));

        GitAdd.run(gitP4ConfigFilePath.toString());
        GitCommit.run("update git p4 config");

        GitTag.tag(lastSubmitTag, "git p4 clone");
        GitCheckout.run(String.format("-b %s", p4IntBranchName));
    }

    @GitP4Operation(option = EmptyOption.class)
    private void sync(EmptyOption option) throws Exception {
        checkWorkingDir();
        if (!Files.exists(gitP4ConfigFilePath)) {
            throw new GitP4Exception("Please run git p4 clone first");
        }
        Properties config = GitP4Config.load(gitP4ConfigFilePath);
        int lastChangelist = Integer.parseInt(config.getProperty(GitP4Config.lastSync)) + 1;
        P4RepositoryInfo repoInfo = new P4RepositoryInfo(config.getProperty(GitP4Config.p4Repo));

        List<P4ChangeInfo> p4Changes = P4Changes.run(String.format(GIT_P4_SYNC_CMD_FMT, repoInfo.getPath(), lastChangelist));
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

        applyP4Changes(p4Changes, repoInfo);
        updateGitP4Config();
    }

    @GitP4Operation(option = SubmitOption.class)
    private void submit(SubmitOption option) throws Exception {
        checkWorkingDir();
        GitLogInfo latest = GitLog.getLatestCommit();
        logger.info("Commits submitted upto " + latest.getCommit());

        final String range = (GitTag.tagExisting(lastSubmitTag)) ?
                String.format("%1$s..%2$s", lastSubmitTag, latest.getCommit()) :
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
        Set<String> rawViews = GitP4Config.getViews(gitP4ConfigFilePath).stream().map(cur -> Paths.get(cur).toString()).collect(Collectors.toSet());
        Set<String> ignorePattern = GitP4Config.getSubmitIgnore(gitP4ConfigFilePath).stream().map(cur -> Paths.get(cur).toString()).collect(Collectors.toSet());
        logger.info(String.format("IGNORE PATTERN:\n%s", StringUtils.join(ignorePattern, "\n")));

        for (GitFileInfo info : files) {
            String file = info.getFile();
            if (Utils.collectionContains(ignorePattern, Paths.get(file).toString()::startsWith)) {
                ignoredFiles.add(file);
                continue;
            }
            if (Utils.collectionContains(rawViews, Paths.get(file).toString()::startsWith)) {
                affectedFiles.add(file);
            } else {
                throw new GitP4Exception(String.format("%s is not under the p4 view map, please update your .gitp4/config", file));
            }
        }

        logger.info(String.format("IGNORED:\n%s", StringUtils.join(ignoredFiles, "\n")));

        final Set<String> finalCopy = affectedFiles;
        Properties config = GitP4Config.load(gitP4ConfigFilePath);
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

        pagedActionOnFiles(gitP4DepotFileMap.values(),
                cur -> P4Fstat.getFileStats(cur).getFiles().forEach(info -> {
                    if (!P4Operation.delete.equals(info.getOperation())) // if the headAction is delete means this file has been deleted on P4
                        p4ExistingFiles.put(info.getDepotFile(), info.getClientFile());
                }),
                "checking p4 files...");

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

        pagedActionOnFiles(editSet, cur -> P4Edit.run(cur, p4cl), "p4 edit...");

        pagedActionOnFiles(deleteSet, cur -> P4Delete.run(cur, p4cl), "p4 delete...");

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
            if (!Utils.runConcurrentlyAndAggregate(MAX_THREADS, theCallable)) {
                throw new RuntimeException("Error occurred when copying files");
            }
        }

        pagedActionOnFiles(addSet, cur -> P4Add.run(cur, p4cl), "p4 add...");
        logger.info(String.format(submitHints, p4cl, latest.getCommit()));
    }

    private static void pagedActionOnFiles(Collection<String> files, Consumer<String> action, String log) throws Exception {
        if (!files.isEmpty()) {
            logger.info(log);
            List<String> filesWithQuotes = files.stream()
                    .map(cur -> String.format("\"%s\"", cur))
                    .collect(Collectors.toList());
            Utils.pagedAction(filesWithQuotes, GIT_ADD_RM_PAGE_SIZE, page -> action.accept(StringUtils.join(page, " ")));
        }
    }

    private void applyP4Changes(List<P4ChangeInfo> p4Changes, P4RepositoryInfo repoInfo) throws Exception {
        int i = 1;
        int total = p4Changes.size();

        Set<String> views = GitP4Config.getViewsWithRoot(gitP4ConfigFilePath);
        if (!views.isEmpty()) {
            logger.info("only files under following directories will be cloned:\n" + StringUtils.join(views, "\n"));
        }

        for (P4ChangeInfo change : p4Changes) {
            logger.info(String.format("[%1$d/%2$d]: %3$s", i++, total, change.toString()));

            copySingleChangelistAndGitCommit(change, repoInfo, views);
        }
    }

    private void copySingleChangelistAndGitCommit(P4ChangeInfo p4Change, P4RepositoryInfo repoInfo, Set<String> views) throws Exception {
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
            Thread.sleep(P4_SYNC_DELAY);
        }

        // git rm files first, to avoid the case that move/delete & move/add in the same changelist, to rename a specific
        // file name. This happens when renaming files in IntelliJ
        pagedActionOnFiles(removeFiles, GitRm::run, "Git rm...");

        for (String target : addFiles) {
            Files.createDirectories(Paths.get(target).toAbsolutePath().getParent());
        }


        // copying file in parallel
        if (!theCallable.isEmpty()) {
            logger.info(String.format("%d file(s) in total to copy...", theCallable.size()));
            if (!Utils.runConcurrentlyAndAggregate(MAX_THREADS, theCallable)) {
                throw new RuntimeException("Error occurred when copying files for changelist " + p4Change.getChangeList());
            }
        }

        pagedActionOnFiles(addFiles, GitAdd::run, "Git add...");

        updateLastSyncAndGitAdd(p4Change.getChangeList());
        String comments = String.format(commitCommentsTemplate,
                info.getDescription(),
                repoInfo.getPath(),
                p4Change.getChangeList(),
                p4Change.getP4UserInfo().toString(),
                p4Change.getDate());

        GitCommit.commitFromFile(comments, p4Change.getChangeList());
    }

    private void updateGitP4Config() throws Exception {
        GitAdd.run(gitP4ConfigFilePath.toString());
        GitCommit.run("commit for .gitp4");
    }

    private void updateLastSyncAndGitAdd(String lastSync) throws Exception {
        Properties config = GitP4Config.load(gitP4ConfigFilePath);
        config.setProperty(GitP4Config.lastSync, lastSync);
        GitP4Config.save(config, gitP4ConfigFilePath, "");
        GitAdd.run(gitP4ConfigFilePath.toString());
    }

    private void checkWorkingDir() {
        try {
            GitP4Config.load(gitP4ConfigFilePath);
        } catch (IOException e) {
            throw new GitP4Exception("Please run this command under the directory where you ran clone");
        }
    }
}
