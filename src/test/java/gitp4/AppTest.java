package gitp4;

import gitp4.git.GitChangeType;
import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import gitp4.p4.*;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void testP4ChangeInfo() {
        final String info = "Change 313596 on 2016/08/22 by EASAP\\chriskang@EASAP_chriskang_ws5 'change on p4: revision 6 '";
        P4ChangeInfo change = P4ChangeInfo.create(info);
        Assert.assertEquals("313596", change.getChangeList());
        Assert.assertEquals("2016/08/22", change.getDate());
        Assert.assertEquals("EASAP\\chriskang@EASAP_chriskang_ws5", change.getP4UserInfo().toString());
        Assert.assertEquals("'change on p4: revision 6 '", change.getComments());
    }

    @Test
    public void testP4FileInfo() {
        String info = "... //nucleus/SANDBOX/testgitp4/createdOnGit.txt#6 edit";

        P4FileInfo fileInfo = P4FileInfo.create(info, "//nucleus/SANDBOX/testgitp4/");

        Assert.assertEquals("//nucleus/SANDBOX/testgitp4/createdOnGit.txt", fileInfo.getFile());
        Assert.assertEquals(6, fileInfo.getRevision());
        Assert.assertEquals(P4Operation.edit, fileInfo.getOperation());

        info = "... //nucleus/SANDBOX/catalog/catalog.ui/src/main/java/com/ea/eadp/catalog/ui/CatalogUIException.java#5 move/delete";
        fileInfo = P4FileInfo.create(info, "//nucleus/SANDBOX/catalog/");
        Assert.assertEquals("//nucleus/SANDBOX/catalog/catalog.ui/src/main/java/com/ea/eadp/catalog/ui/CatalogUIException.java", fileInfo.getFile());
        Assert.assertEquals(5, fileInfo.getRevision());
        Assert.assertEquals(P4Operation.delete, fileInfo.getOperation());

        Assert.assertNull(P4FileInfo.create(info, "//abc"));
    }

    @Test
    public void testP4ChangelistInfo() {
        final String info = "Change 313596 by EASAP\\chriskang@EASAP_chriskang_ws5 on 2016/08/22 02:57:56\n" +
                "\n" +
                "        change on p4: revision 6\nABC" +
                "\n" +
                "Affected files ...\n" +
                "\n" +
                "... //nucleus/SANDBOX/testgitp4/createdOnGit.txt#6 edit\n" +
                "... //nucleus/SANDBOX/testgitp4/abc.txt#6 edit\n" +
                "... //nucleus/SANDBOX/abc/abc.txt#6 edit";
        P4ChangeListInfo clInfo = new P4ChangeListInfo(Arrays.asList(StringUtils.split(info, '\n')), "//nucleus/SANDBOX/testgitp4/");
        Assert.assertEquals("313596", clInfo.getChangelist());
        Assert.assertEquals("2016/08/22 02:57:56", clInfo.getTimestamp());
        Assert.assertEquals("EASAP\\chriskang", clInfo.getP4UserInfo().getUser());
        Assert.assertEquals("EASAP_chriskang_ws5", clInfo.getP4UserInfo().getWorkspace());
        Assert.assertEquals("        change on p4: revision 6\nABC", clInfo.getFullComments());
        Assert.assertEquals(2, clInfo.getFiles().size());
    }

    @Test
    public void testP4FileOpenInfo() {
        String info = "//nucleus/SANDBOX/catalog/sandboxLoader.sh#1 - edit default change (xtext)";
        P4FileOpenedInfo foInfo = P4FileOpenedInfo.create(info);
        Assert.assertEquals("//nucleus/SANDBOX/catalog/sandboxLoader.sh", foInfo.getFile());

        info = "//nucleus/SANDBOX/catalog - file(s) not opened on this client.";
        foInfo = P4FileOpenedInfo.create(info);
        Assert.assertNull(foInfo);
    }

    @Test
    public void testGitLogInfo() {
        final String cmdRes = "251adbef66f2db998f88c4833ad521877b521955  change on p4: revision 6 [git-p4 depot-paths = //nucleus/SANDBOX/testgitp4/: change = 313596]";
        GitLogInfo info = new GitLogInfo(cmdRes);
        Assert.assertEquals("251adbef66f2db998f88c4833ad521877b521955", info.getCommit());
        Assert.assertEquals("change on p4: revision 6 [git-p4 depot-paths = //nucleus/SANDBOX/testgitp4/: change = 313596]", info.getComment());
    }

    @Test
    public void testGitFileInfo() {

        String add = "A       src/main/java/gitp4/GitP4Operation.java";
        GitFileInfo info = new GitFileInfo(add);
        Assert.assertEquals(GitChangeType.Add, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/GitP4Operation.java", info.getFile());

        add = "A\tcatalog/testData/smoke/lookup/GameEditionTypeFacetKey/Anniversary Edition.xml";
        info = new GitFileInfo(add);
        Assert.assertEquals(GitChangeType.Add, info.getChangeType());
        Assert.assertEquals("catalog/testData/smoke/lookup/GameEditionTypeFacetKey/Anniversary Edition.xml", info.getFile());

        final String modify = "M       src/main/java/gitp4/p4/P4RepositoryInfo.java";
        info = new GitFileInfo(modify);
        Assert.assertEquals(GitChangeType.Modify, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getFile());

        final String delete = "D\t src/main/java/gitp4/p4/P4RepositoryInfo.java";
        info = new GitFileInfo(delete);
        Assert.assertEquals(GitChangeType.Delete, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getFile());

        Exception exp = null;
        try {
            final String move = "R087\tsrc/main/java/gitp4/p4/P4Change.java\tsrc/main/java/gitp4/p4/P4ChangeInfo.java";
            info = new GitFileInfo(move);
            Assert.assertEquals(GitChangeType.Rename, info.getChangeType());
            Assert.assertEquals("src/main/java/gitp4/p4/P4Change.java", info.getFile());


        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                exp = e;
            } else {
                exp = null;
            }
        }
        Assert.assertNotNull(exp);
    }

    @Test
    public void testP4FileStatsInfo() {
        final String input = "... depotFile //nucleus/SANDBOX/catalog/tomcat.app/src/main/webapp/config/devbox/search.properties\n" +
                "... clientFile D:\\EASAP_chriskang_EASHDPDESK075_70\\nucleus\\SANDBOX\\catalog\\tomcat.app\\src\\main\\webapp\\config\\devbox\\search.properties\n" +
                "... isMapped \n" +
                "... headAction integrate\n" +
                "... headType text\n" +
                "... headTime 1420531837\n" +
                "... headRev 5\n" +
                "... headChange 282987\n" +
                "... headModTime 1400056319\n" +
                "... haveRev 1\n" +
                "\n" +
                "... depotFile //nucleus/SANDBOX/catalog/tomcat.app/src/main/webapp/config/prod/mysql.properties\n" +
                "... clientFile D:\\EASAP_chriskang_EASHDPDESK075_70\\nucleus\\SANDBOX\\catalog\\tomcat.app\\src\\main\\webapp\\config\\prod\\mysql.properties\n" +
                "... isMapped \n" +
                "... headAction move/add\n" +
                "... headType text\n" +
                "... headTime 1420531837\n" +
                "... headRev 6\n" +
                "... headChange 282987\n" +
                "... headModTime 1418196382\n" +
                "... haveRev 1\n" +
                "\n" +
                "... desc Merging\n" +
                "\n" +
                "//nucleus/NNG/catalog/...\n" +
                "\n" +
                "to //nucleus/SANDBOX/catalog/...\n" +
                "\n" +
                "\n";
        P4FileStatInfo info = P4FileStatInfo.create(Arrays.asList(StringUtils.split(input, "\n")));
        final String des = "Merging\n//nucleus/NNG/catalog/...\nto //nucleus/SANDBOX/catalog/...";
        Assert.assertEquals(des, info.getDescription());
        Assert.assertEquals(2, info.getFiles().size());
        P4FileInfoEx file = new P4FileInfoEx(
                "//nucleus/SANDBOX/catalog/tomcat.app/src/main/webapp/config/devbox/search.properties",
                "D:\\EASAP_chriskang_EASHDPDESK075_70\\nucleus\\SANDBOX\\catalog\\tomcat.app\\src\\main\\webapp\\config\\devbox\\search.properties",
                P4Operation.integrate,
                5, 282987);
        Assert.assertEquals(file, info.getFiles().get(0));
        file = new P4FileInfoEx(
                "//nucleus/SANDBOX/catalog/tomcat.app/src/main/webapp/config/prod/mysql.properties",
                "D:\\EASAP_chriskang_EASHDPDESK075_70\\nucleus\\SANDBOX\\catalog\\tomcat.app\\src\\main\\webapp\\config\\prod\\mysql.properties",
                P4Operation.add,
                6, 282987);
        Assert.assertEquals(file, info.getFiles().get(1));
    }

    @Test
    public void testP4RepoInfo() throws IOException {
        P4RepositoryInfo info = new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...");
        Assert.assertEquals("//nucleus/SANDBOX/catalog/", info.getPath());
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", info.getPathWithSubContents());

        info = new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...@265261");
        Assert.assertEquals("//nucleus/SANDBOX/catalog/", info.getPath());
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", info.getPathWithSubContents());

        info = new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...#head");
        Assert.assertEquals("//nucleus/SANDBOX/catalog/", info.getPath());
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", info.getPathWithSubContents());

        info = new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...@265261,#head");
        Assert.assertEquals("//nucleus/SANDBOX/catalog/", info.getPath());
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", info.getPathWithSubContents());

        Exception exp = null;
        try {
            new P4RepositoryInfo("//nucleus/SANDBOX/catalog...");
        } catch (GitP4Exception e) {
            exp = e;
        }
        Assert.assertNotNull(exp);


        try {
            new P4RepositoryInfo("//nucleus/SANDBOX/catalog...@265261,@265261");
        } catch (GitP4Exception e) {
            exp = e;
        }
        Assert.assertNotNull(exp);


        exp = null;
        try {
            new P4RepositoryInfo("//nucleus/SANDBOX/catalog/");
        } catch (GitP4Exception e) {
            exp = e;
        }
        Assert.assertNotNull(exp);

        exp = null;
        try {
            new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...@123, @234");
        } catch (GitP4Exception e) {
            exp = e;
        }
        Assert.assertNotNull(exp);

        exp = null;
        try {
            new P4RepositoryInfo("//nucleus/SANDBOX/catalog/...#head,@234");
        } catch (GitP4Exception e) {
            exp = e;
        }
        Assert.assertNotNull(exp);
    }

    @Test
    public void testUtilsConvertToArgArray() {
        String cmd = "git commit -mabc";
        String[] res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(3, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-mabc", res[2]);

        cmd = "git commit -m abc";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(4, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-m", res[2]);
        Assert.assertEquals("abc", res[3]);

        cmd = "git   commit    -m      abc";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(4, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-m", res[2]);
        Assert.assertEquals("abc", res[3]);

        cmd = "git commit -m 'abc'";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(4, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-m", res[2]);
        Assert.assertEquals("abc", res[3]);

        cmd = "git commit -m \"abc\"";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(4, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-m", res[2]);
        Assert.assertEquals("abc", res[3]);

        cmd = "git commit -m\"abc\"";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(4, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("commit", res[1]);
        Assert.assertEquals("-m", res[2]);
        Assert.assertEquals("abc", res[3]);

        cmd = "git add 'abc' 'def' \"xy z\"";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(5, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("add", res[1]);
        Assert.assertEquals("abc", res[2]);
        Assert.assertEquals("def", res[3]);
        Assert.assertEquals("xy z", res[4]);

        cmd = "git log --pretty=\"\" --name-status last_p4_submit..abcde";
        res = Utils.convertToArgArray(cmd);
        Assert.assertEquals(6, res.length);
        Assert.assertEquals("git", res[0]);
        Assert.assertEquals("log", res[1]);
        Assert.assertEquals("--pretty=", res[2]);
        Assert.assertEquals("", res[3]);
        Assert.assertEquals("--name-status", res[4]);
        Assert.assertEquals("last_p4_submit..abcde", res[5]);
    }

    @Test
    public void testUtilsCollectionContains() {
        Set<String> pattern = new HashSet<String>() {{
            add("a");
            add("e/x");
        }};
        Map<String, Boolean> files = new HashMap<String, Boolean>() {{
            put("a/b/c", true);
            put("a/d", true);
            put("b/x/y", false);
            put("e/x", true);
            put("c/x/y", false);
            put("e/f/y", false);
            put("e/x/y", true);
        }};

        for (Map.Entry<String, Boolean> entry : files.entrySet()) {
            boolean expected = entry.getValue();
            Assert.assertEquals(String.format("file: %1$s", entry.getKey()), expected, Utils.collectionContains(pattern, entry.getKey()::startsWith));
        }
    }
}
