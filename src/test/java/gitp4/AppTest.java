package gitp4;


import gitp4.git.GitChangeType;
import gitp4.git.GitFileInfo;
import gitp4.git.GitLogInfo;
import gitp4.p4.P4ChangeInfo;
import gitp4.p4.P4ChangeListInfo;
import gitp4.p4.P4FileInfo;
import gitp4.p4.P4Operation;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;

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
        final String info = "... //nucleus/SANDBOX/testgitp4/createdOnGit.txt#6 edit";
        P4FileInfo fileInfo = new P4FileInfo(info);
        Assert.assertEquals("//nucleus/SANDBOX/testgitp4/createdOnGit.txt", fileInfo.getFile());
        Assert.assertEquals(6, fileInfo.getRevision());
        Assert.assertEquals(P4Operation.edit, fileInfo.getOperation());
    }

    @Test
    public void testP4ChangelistInfo() {
        final String info = "Change 313596 by EASAP\\chriskang@EASAP_chriskang_ws5 on 2016/08/22 02:57:56\n" +
                "\n" +
                "        change on p4: revision 6\nABC" +
                "\n" +
                "Affected files ...\n" +
                "\n" +
                "... //nucleus/SANDBOX/testgitp4/createdOnGit.txt#6 edit";
        P4ChangeListInfo clInfo = new P4ChangeListInfo(Arrays.asList(StringUtils.split(info, '\n')));
        Assert.assertEquals("313596", clInfo.getChangelist());
        Assert.assertEquals("2016/08/22 02:57:56", clInfo.getTimestamp());
        Assert.assertEquals("EASAP\\chriskang", clInfo.getP4UserInfo().getUser());
        Assert.assertEquals("EASAP_chriskang_ws5", clInfo.getP4UserInfo().getWorkspace());
        Assert.assertEquals("        change on p4: revision 6\nABC", clInfo.getFullComments());
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
