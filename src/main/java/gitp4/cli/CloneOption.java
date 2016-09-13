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
}
