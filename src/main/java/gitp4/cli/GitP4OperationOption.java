package gitp4.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

/**
 * Created by chriskang on 9/9/2016.
 */
public abstract class GitP4OperationOption {
    protected Options options = new Options();
    protected CommandLine line;
    private final String[] args;

    public GitP4OperationOption(String[] args) {
        this.args = args;
        options.addOption("h", "help", false, "print this message");
    }

    final public Options getOptions() {
        return this.options;
    }

    final public void parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            Logger.getLogger(this.getClass()).error("Parsing failed: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("init", options);
        }

    }

}
