package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 9/6/2016.
 */
class P4Modify {
    enum ModifyAction {
        add,
        edit,
        delete
    }

    private static final String CMD_FMT = Utils.getArgFormat("p4 %3$s -c %1$s %2$s");
    private static final String X_CMD_FMT = Utils.getArgFormat("%1$s -c %2$s");

    public static void run(ModifyAction action, String file, String changelist) {
        if (action == null) throw new NullPointerException("action");
        if (StringUtils.isBlank(file)) throw new NullPointerException("file");
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        CmdRunner.getP4CmdRunner().run(() -> Utils.convertToArgArray(String.format(CMD_FMT, changelist, file, action)), cmdRes -> "");
    }

    public static void batch(ModifyAction action, Iterable<? extends CharSequence> files, String changelist) throws Exception {
        if (action == null) throw new NullPointerException("action");
        if (files == null) throw new NullPointerException("files");
        if (StringUtils.isBlank(changelist)) throw new NullPointerException("changelist");
        P4XTemplate.run(String.format(X_CMD_FMT, action, changelist), action.toString(), files, cmdRes -> "");
    }
}
