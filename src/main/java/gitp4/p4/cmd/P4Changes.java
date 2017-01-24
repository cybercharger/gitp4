package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import gitp4.p4.P4ChangeInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Changes {
    public static final String P4_CHANGES_CMD = Utils.getArgFormat("p4 changes %s");

    public static List<P4ChangeInfo> run(final String parameters) {
        String cmdParams = StringUtils.isBlank(parameters) ? "" : parameters;

        return CmdRunner.getP4CmdRunner().run(() -> Utils.convertToArgArray(String.format(P4_CHANGES_CMD, cmdParams)),
                (cmdRes) -> {
                    LinkedList<P4ChangeInfo> result = new LinkedList<>();
                    cmdRes.forEach(cur -> result.add(0, P4ChangeInfo.create(cur)));
                    return result;
                });
    }
}

