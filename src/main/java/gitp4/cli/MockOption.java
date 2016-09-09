package gitp4.cli;

/**
 * Created by chriskang on 9/9/2016.
 */
public class MockOption extends GitP4OperationOption {
    public MockOption(String[] args) {
        super(args);
        super.options.addOption("m", "mock", false, "mock");
    }

}
