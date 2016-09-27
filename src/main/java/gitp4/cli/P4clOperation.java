package gitp4.cli;

import org.apache.commons.cli.Option;

/**
 * Created by chriskang on 9/26/2016.
 */
public class P4clOperation extends GitP4OperationOption {
    private final static String MSG_ARG = "message";
    private final static String DRYRUN_ARG = "dry-run";
    private final static String FORCE_ARG = "force";

    public P4clOperation(String[] args) {
        super("p4cl", args);
        super.options.addOption(Option.builder("m")
                .argName(MSG_ARG)
                .longOpt(MSG_ARG)
                .hasArg()
                .required()
                .build());

        super.options.addOption(Option.builder("n")
                .argName(DRYRUN_ARG)
                .longOpt(DRYRUN_ARG)
                .build());

        super.options.addOption(Option.builder()
                .argName(FORCE_ARG)
                .longOpt(FORCE_ARG)
                .build());
    }

    public String getMessage() {
        return line.hasOption(MSG_ARG) ? line.getOptionValue(MSG_ARG) : null;
    }

    public boolean dryrun() {
        return line.hasOption(DRYRUN_ARG);
    }

    public boolean isForced() {
        return line.hasOption(FORCE_ARG);
    }
}
