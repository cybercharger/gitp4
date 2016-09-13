package gitp4.p4.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 9/7/2016.
 */
public class P4Delete {
    private static final String CMD_FMT = "p4 delete -c %1$s %2$s";

    public static void run(String files, String changelist) {
        if (StringUtils.isBlank(files)) throw new NullPointerException("files");
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        CmdRunner.getP4CmdRunner().run(() -> String.format(CMD_FMT, changelist, files), cmdRes -> "");
    }
}
