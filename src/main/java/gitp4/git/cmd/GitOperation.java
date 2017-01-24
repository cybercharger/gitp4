package gitp4.git.cmd;

import gitp4.CmdRunner;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by chriskang on 1/23/2017.
 */
public class GitOperation {
    enum Operation {
        Add("add"),
        Remove("rm"),
        Checkout("checkout");

        private String opStr;

        Operation(String opStr) {
            this.opStr = opStr;
        }

        public String getOpStr() {
            return opStr;
        }
    }

    private static final String GIT_CMD = "git";

    private static final int PREFIX_LEN = 2;

    public static void run(final Operation operation, final List<String> args) {
        if (operation == null) throw new NullPointerException("operation");
        if (args == null || args.isEmpty()) throw new NullPointerException("args");
        String[] cmd = new String[args.size() + PREFIX_LEN];
        cmd[0] = GIT_CMD;
        cmd[1] = operation.getOpStr();
        String[] fileArray = args.toArray(new String[args.size()]);
        System.arraycopy(fileArray, 0, cmd, PREFIX_LEN, fileArray.length);
        CmdRunner.getGitCmdRunner().run(() -> cmd, (cmdRes) -> "");
    }

    public static void run(final Operation operation, final String arg) {
        if (operation == null) throw new NullPointerException("operation");
        if (StringUtils.isBlank(arg)) throw new NullPointerException("arg");
        String[] cmd = new String[1 + PREFIX_LEN];
        cmd[0] = GIT_CMD;
        cmd[1] = operation.getOpStr();
        cmd[2] = arg;
        CmdRunner.getGitCmdRunner().run(() -> cmd, (cmdRes) -> "");
    }
}
