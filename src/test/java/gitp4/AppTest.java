package gitp4;


import gitp4.console.Progress;
import gitp4.git.GitChangeType;
import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import gitp4.p4.*;
import junit.framework.Assert;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void testP4Change() {
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
        final String add = "A       src/main/java/gitp4/GitP4Operation.java";
        GitFileInfo info = new GitFileInfo(add);
        Assert.assertEquals(GitChangeType.Add, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/GitP4Operation.java", info.getOldFile());
        Assert.assertEquals("src/main/java/gitp4/GitP4Operation.java", info.getNewFile());

        final String move = "R087\tsrc/main/java/gitp4/p4/P4Change.java\tsrc/main/java/gitp4/p4/P4ChangeInfo.java";
        info = new GitFileInfo(move);
        Assert.assertEquals(GitChangeType.Rename, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/p4/P4Change.java", info.getOldFile());
        Assert.assertEquals("src/main/java/gitp4/p4/P4ChangeInfo.java", info.getNewFile());

        final String modify = "M       src/main/java/gitp4/p4/P4RepositoryInfo.java";
        info = new GitFileInfo(modify);
        Assert.assertEquals(GitChangeType.Modify, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getOldFile());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getNewFile());

        final String delete = "D\t src/main/java/gitp4/p4/P4RepositoryInfo.java";
        info = new GitFileInfo(delete);
        Assert.assertEquals(GitChangeType.Delete, info.getChangeType());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getOldFile());
        Assert.assertEquals("src/main/java/gitp4/p4/P4RepositoryInfo.java", info.getNewFile());
    }
}
