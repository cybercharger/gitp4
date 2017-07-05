package gitp4;

public class App {
    public static void main(String[] args) throws Exception {
        String missed = EnvironmentVariables.verify();
        if (missed != null) {
            System.err.println(missed);
            System.exit(-1);
        }
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.operate(args);
    }
}
