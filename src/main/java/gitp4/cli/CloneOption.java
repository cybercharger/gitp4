package gitp4.cli;

import gitp4.GitP4Config;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chriskang on 9/9/2016.
 */
public class CloneOption extends GitP4OperationOption {
    private final static String VIEW_ARG = "view-map";

    private final static String NO_EMPTY_CHK_ARG = "no-empty-check";

    private final static String P4_INT_BRANCH = "p4-integ";
    public final static String DEF_P4_INT_BRANCH = "p4-integ";
    public final static String P4_INT_BRANCH_NAME_FMT = "%1$s-%2$s";

    private final static String SUBMIT_BRANCH_NAME = "p4-submit";
    public final static String DEF_SUBMIT_BRANCH_NAME = "master";

    public CloneOption(String[] args) {
        super("clone", args);

        super.options.addOption(Option.builder("v")
                .longOpt(VIEW_ARG)
                .argName(VIEW_ARG)
                .hasArg()
                .desc("comma separated list of sub directories, NO SPACE")
                .build());

        super.options.addOption(Option.builder()
                .longOpt(NO_EMPTY_CHK_ARG)
                .argName(NO_EMPTY_CHK_ARG)
                .desc("no empty check on current directory to clone p4 repository")
                .build());

        super.options.addOption(Option.builder()
                .longOpt(P4_INT_BRANCH)
                .argName(P4_INT_BRANCH)
                .hasArg()
                .desc("p4 integration branch name to create after clone is finished")
                .build());

        super.options.addOption(Option.builder()
                .longOpt(SUBMIT_BRANCH_NAME)
                .argName(SUBMIT_BRANCH_NAME)
                .hasArg()
                .desc("from which branch git changes should be submitted to p4")
                .build());
    }

    @Override
    protected void onParse() throws ParseException {
        String[] args = line.getArgs();
        if (args == null || args.length != 1) {
            throw new ParseException("clone target is not correctly provided");
        }
    }

    public String getCloneString() {
        if (line == null) throw new NullPointerException("line");
        return line.getArgs()[0];
    }

    public List<String> getViewMap() {
        if (line == null) throw new NullPointerException("line");
        if (!line.hasOption(VIEW_ARG)) return Collections.emptyList();
        String viewString = line.getOptionValue(VIEW_ARG);
        if (viewString == null) throw new IllegalArgumentException(String.format("arg is not proved for %s", VIEW_ARG));

        return Arrays.stream(StringUtils.split(viewString, GitP4Config.COMMA)).map(String::trim).collect(Collectors.toList());
    }

    public String getViewString() {
        return line.hasOption(VIEW_ARG) ? line.getOptionValue(VIEW_ARG) : "";
    }

    public boolean bypassEmptyCheck() {
        return line.hasOption(NO_EMPTY_CHK_ARG);
    }

    public String getP4IntBranchName() {
        String branch = line.hasOption(P4_INT_BRANCH) ? line.getOptionValue(P4_INT_BRANCH) : DEF_P4_INT_BRANCH;
        String profile = super.getProfile();
        return StringUtils.isBlank(profile) ? branch : String.format(P4_INT_BRANCH_NAME_FMT, profile, branch);
    }

    public String getSubmitBranchName() {
        return line.hasOption(SUBMIT_BRANCH_NAME) ? line.getOptionValue(SUBMIT_BRANCH_NAME) : DEF_SUBMIT_BRANCH_NAME;
    }
}
