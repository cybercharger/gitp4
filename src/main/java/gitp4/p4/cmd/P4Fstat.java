package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.TempFileManager;
import gitp4.p4.P4FileOpenedInfo;
import gitp4.p4.P4FileStatInfo;
import gitp4.p4.P4RepositoryInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by chriskang on 9/1/2016.
 */
public class P4Fstat {
    private static final String FSTAT_CL_CMD = "p4 fstat -e %1$s %2$s";
    private static final String FSTAT_FILE_CMD = "p4 fstat %s";
    private static final String FSTAT_FILE_X_CMD = "p4 -x %s fstat";
    private static final String FSTAT_CMD = "fstat";

    public static P4FileStatInfo getChangelistStats(String changlist, P4RepositoryInfo repoInfo) {
        if (StringUtils.isBlank(changlist)) throw new NullPointerException("changelist");
        if (repoInfo == null) throw new NullPointerException("repoInfo");
        return CmdRunner.getP4CmdRunner().run(() -> String.format(FSTAT_CL_CMD, changlist, repoInfo.getPathWithSubContents()), P4FileStatInfo::create);
    }

    public static P4FileStatInfo getFileStats(String files) {
        if (StringUtils.isBlank(files)) throw new NullPointerException("changelist");
        return CmdRunner.getP4CmdRunner().run(() -> String.format(FSTAT_FILE_CMD, files), P4FileStatInfo::create);
    }

    public static P4FileStatInfo batchGetFileStats(Iterable<? extends CharSequence> files) throws Exception {
        return P4XTemplate.run(FSTAT_CMD, FSTAT_CMD, files, P4FileStatInfo::create);
    }

    public static P4FileStatInfo batchGetFileStatsAfter(Iterable<? extends CharSequence> files, String changelist) throws Exception {
        return P4XTemplate.run(FSTAT_CMD, FSTAT_CMD, files, P4FileStatInfo::create);
    }
}
