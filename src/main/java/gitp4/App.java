package gitp4;

public class App {
    public static void main(String[] args) throws Exception {
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.operate(args);
    }
}
