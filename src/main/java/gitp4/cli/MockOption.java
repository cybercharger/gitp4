package gitp4.cli;


import org.apache.commons.cli.Option;

/**
 * Created by chriskang on 9/9/2016.
 */
public class MockOption extends GitP4OperationOption {
    private static final String MOCK_ARG = "mock";
    public MockOption(String[] args) {
        super("mock", args);
        Option mock = new Option("m", MOCK_ARG, true, "submit message");
        mock.setArgName(MOCK_ARG);
        mock.setRequired(true);
        super.options.addOption(mock);
    }

    public String getMock() {
        return line.hasOption(MOCK_ARG) ? line.getOptionValue(MOCK_ARG) : null;
    }
}
