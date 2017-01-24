package gitp4.p4.cmd;

import gitp4.CmdRunner;
import gitp4.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by chriskang on 8/23/2016.
 */
public class P4Print {
    public static final String P4_PRINT_CMD = Utils.getArgFormat("p4 print -q -o %1$s %2$s");

    public static void run(final String p4File, final String outputFile) {
        if (StringUtils.isBlank(p4File)) throw new NullPointerException("p4File");
        if (StringUtils.isBlank(outputFile)) throw new NullPointerException("outputFile");
        CmdRunner.getP4CmdRunner().run(() -> Utils.convertToArgArray(String.format(P4_PRINT_CMD, outputFile, p4File)), (cmdRes) -> "");
    }
}
