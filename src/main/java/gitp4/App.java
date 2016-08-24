package gitp4;

import org.apache.log4j.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.clone("//nucleus/SANDBOX/testgitp4");
    }
}
