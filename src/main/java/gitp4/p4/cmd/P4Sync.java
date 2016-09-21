package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.p4.P4RepositoryInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/31/2016.
 */
public class P4Sync {
    private static final String FORCE_SYNC_TO_CMD = "p4 sync -f %1$s@=%2$s";
    private static final String SYNC_TO_LATEST_CMD = "p4 sync %1$s";

    public static void forceSyncTo(P4RepositoryInfo repoInfo, String changelist) {
        if (repoInfo == null) throw new NullPointerException("repoInfo");
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        CmdRunner.getP4CmdRunner().run(() -> String.format(FORCE_SYNC_TO_CMD, repoInfo.getPathWithSubContents(), changelist), cmdRes -> "");
    }

    public static void syncToLatest(P4RepositoryInfo repoInfo) {
        if (repoInfo == null) throw new NullPointerException("repoInfo");
        CmdRunner.getP4CmdRunner().run(() -> String.format(SYNC_TO_LATEST_CMD, repoInfo.getPathWithSubContents()), cmdRes -> "");
    }
}
