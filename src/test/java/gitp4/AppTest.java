package gitp4;


import gitp4.p4.P4Change;
import gitp4.p4.P4ChangeListInfo;
import gitp4.p4.P4FileInfo;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void testP4Change() {
        final String info = "Change 313596 on 2016/08/22 by EASAP\\chriskang@EASAP_chriskang_ws5 'change on p4: revision 6 '";
        P4Change change = P4Change.create(info);
        Assert.assertEquals("313596", change.getChangeList());
        Assert.assertEquals("2016/08/22", change.getDate());
        Assert.assertEquals("EASAP\\chriskang@EASAP_chriskang_ws5", change.getP4UserInfo());
        Assert.assertEquals("'change on p4: revision 6 '", change.getComments());
    }

    @Test
    public void testP4FileInfo() {
        final String info = "... //nucleus/SANDBOX/testgitp4/createdOnGit.txt#6 edit";
        P4FileInfo fileInfo = new P4FileInfo(info);
        Assert.assertEquals("//nucleus/SANDBOX/testgitp4/createdOnGit.txt", fileInfo.getFile());
        Assert.assertEquals(6, fileInfo.getRevision());
        Assert.assertEquals("edit", fileInfo.getOperation());
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
}
