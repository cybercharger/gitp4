package gitp4.cli;

import gitp4.GitP4Exception;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by chriskang on 9/9/2016.
 */
public abstract class GitP4OperationOption {
    private static final String HELP_ARG = "help";
    protected Options options = new Options();
    protected CommandLine line;

    private final String[] args;
    private final String cmd;

    public GitP4OperationOption(String cmd, String[] args) {
        if (StringUtils.isBlank(cmd)) throw new NullPointerException("cmd");
        this.cmd = cmd;
        this.args = args;
    }

    final public Options getOptions() {
        return this.options;
    }

    final public void parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
           HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(cmd, options);
            Logger.getLogger(this.getClass()).error("Parsing failed: " + e.getMessage());
            throw new GitP4Exception(e.getMessage());
        }

    }

}
