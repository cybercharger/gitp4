package gitp4;

import gitp4.cli.*;
import gitp4.console.Progress;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by chriskang on 8/26/2016.
 */
public class GitP4BridgeTest {
    private final PrintStream original = System.out;
    private ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    @Before
    public void prepare() {
        System.setOut(new PrintStream(myOut));
    }

    @After
    public void recover() throws IOException {
        myOut.close();
        System.out.close();
        System.setOut(original);
    }

    @Test
    public void testGitP4BridgeMock() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.operate(new String[] {"mock",
                "--mock", "mock-message",
                "--profile", "default",
                "--max-thread", "20",
                "--page-size","100",
                "--p4-sync-delay", "30"});
        String expected = "mock:\t\tmock-message\nprofile:\tdefault\nMaxThreads:\t20\nPageSize:\t100\nP4SyncDelay:\t30ms\n";
        Assert.assertEquals(expected, myOut.toString());
    }

    @Test
    public void testFilterFindFirst() {
        List<Integer> list = Arrays.asList(1, 10, 3, 7, 5);
        Optional<Integer> a = list.stream().map(x -> {
            System.out.print(x + ",");
            return x;
        }).filter(x -> x > 5).findFirst();
        System.out.print(a.isPresent() ? a.get() : new Integer(0));
        Assert.assertEquals("1,10,10", myOut.toString());
    }

    @Test
    public void testOptions() {
        String[] args = new String[] {"-v", "views", "//nucleus/SANDBOX/catalog/..."};
        GitP4OperationOption option = new CloneOption(args);
        option.parse();
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", ((CloneOption)option).getCloneString());
        Assert.assertEquals("views", ((CloneOption)option).getViewString());
        Assert.assertEquals(CloneOption.DEF_P4_INT_BRANCH, ((CloneOption)option).getP4IntBranchName());
        Assert.assertEquals(CloneOption.DEF_SUBMIT_BRANCH_NAME, ((CloneOption)option).getSubmitBranchName());

        args = new String[] {"//nucleus/SANDBOX/catalog/...", "--view-map", "views", "-p", "test", "--p4-submit", "abc"};
        option = new CloneOption(args);
        option.parse();
        Assert.assertEquals("//nucleus/SANDBOX/catalog/...", ((CloneOption)option).getCloneString());
        Assert.assertEquals("views", ((CloneOption)option).getViewString());
        Assert.assertEquals(String.format(CloneOption.P4_INT_BRANCH_NAME_FMT, "test", CloneOption.DEF_P4_INT_BRANCH), ((CloneOption)option).getP4IntBranchName());
        Assert.assertEquals("abc", ((CloneOption)option).getSubmitBranchName());

        args = new String[] {"--message", "cl comments"};
        option = new P4clOperation(args);
        option.parse();
        Assert.assertEquals("cl comments", ((P4clOperation)option).getMessage());

        args = new String[] {"-m", "cl comments"};
        option = new P4clOperation(args);
        option.parse();
        Assert.assertEquals("cl comments", ((P4clOperation)option).getMessage());

        args = new String[] {"-f"};
        option = new SubmitOption(args);
        option.parse();
        Assert.assertTrue(((SubmitOption)option).isForced());

        args = new String[] {"-m", "mock message"};
        option = new MockOption(args);
        option.parse();
        Assert.assertEquals("mock message", ((MockOption)option).getMock());
    }
}
