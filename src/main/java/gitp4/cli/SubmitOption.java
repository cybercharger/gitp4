package gitp4.cli;

import org.apache.commons.cli.Option;

/**
 * Created by chriskang on 9/9/2016.
 */
public class SubmitOption extends GitP4OperationOption {
    private static final String FORCE_CMD = "force";
    public SubmitOption(String[] args) {
        super("submit", args);

        super.options.addOption(Option.builder("f")
                .argName(FORCE_CMD)
                .longOpt(FORCE_CMD)
                .build());
    }

    public boolean isForced() {
        return line.hasOption(FORCE_CMD);
    }
}
