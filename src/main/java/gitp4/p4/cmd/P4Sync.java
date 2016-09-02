package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4RepositoryInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/31/2016.
 */
public class P4Sync {
    private static final String FORCE_SYNC_TO_CMD = "p4 sync -f %1$s@=%2$s";

    public static void forceSyncTo(P4RepositoryInfo repoInfo, String changelist) throws Exception {
        if (repoInfo == null) throw new NullPointerException("repoInfo");
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        CmdRunner.run(() -> String.format(FORCE_SYNC_TO_CMD, repoInfo.getPathWithSubContents(), changelist), cmdRes -> "");
    }
}
