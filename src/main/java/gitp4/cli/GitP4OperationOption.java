package gitp4.cli;

import gitp4.GitP4Exception;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by chriskang on 9/9/2016.
 */
public abstract class GitP4OperationOption {
    protected static final String HELP_ARG = "help";
    private final Options helpOptions = new Options();
    protected final Options options = new Options();
    protected CommandLine line;

    private final String[] args;
    private final String cmd;

    public GitP4OperationOption(String cmd, String[] args) {
        if (StringUtils.isBlank(cmd)) throw new NullPointerException("cmd");
        this.cmd = cmd;
        this.args = args;
        Option helpOption = Option.builder("h").argName(HELP_ARG).longOpt(HELP_ARG).desc("Show this help").required().build();
        helpOptions.addOption(helpOption);
    }

    final public Options getOptions() {
        return this.options;
    }

    final public boolean parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            line = parser.parse(helpOptions, args);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(cmd, options);
            return false;
        } catch (ParseException e) {
            try {
                line = parser.parse(options, args, false);
                onParse();
            } catch (ParseException exp) {
                Logger.getLogger(this.getClass()).error("Parsing failed: " + exp.getMessage());
                throw new GitP4Exception(exp.getMessage());
            }
        }

        return true;
    }

    protected void onParse() throws ParseException {
    }

}
