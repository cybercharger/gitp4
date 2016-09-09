package gitp4.cli;

import org.apache.commons.cli.Option;

/**
 * Created by chriskang on 9/9/2016.
 */
public class SubmitOption extends GitP4OperationOption {
    private final static String MSG_ARG = "message";

    public SubmitOption(String[] args) {
        super("submit", args);
        Option comment = new Option("m", MSG_ARG, true, "submit message");
        comment.setArgName(MSG_ARG);
        comment.setRequired(true);
        super.options.addOption(comment);
    }

    public String getMessage() {
        return line.hasOption(MSG_ARG) ? line.getOptionValue(MSG_ARG) : null;
    }
}
