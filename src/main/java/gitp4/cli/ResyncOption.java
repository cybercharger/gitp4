package gitp4.cli;

import org.apache.commons.cli.Option;

/**
 * Created by chriskang on 10/12/2016.
 */
public class ResyncOption extends GitP4OperationOption {
    private final static String START_ARG = "start";
    private final static String END_ARG = "end";

    public ResyncOption(String[] args) {
        super("re-sync", args);

        super.options.addOption(Option.builder()
                .longOpt(START_ARG)
                .argName(START_ARG)
                .hasArg()
                .type(int.class)
                .desc("start changelist, inclusive")
                .required()
                .build());

        super.options.addOption(Option.builder()
                .longOpt(END_ARG)
                .argName(END_ARG)
                .hasArg()
                .type(int.class)
                .desc("end changelist, inclusive")
                .required()
                .build());
    }

    public int getStartChangelist() {
        return Integer.parseInt(line.getOptionValue(START_ARG));
    }

    public int getEndChangelist() {
        return Integer.parseInt(line.getOptionValue(END_ARG));
    }
}
