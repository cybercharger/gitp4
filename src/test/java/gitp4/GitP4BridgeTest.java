package gitp4;

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
    public void testGitP4BridgeMock() throws InvocationTargetException, IllegalAccessException {
        GitP4Bridge bridge = new GitP4Bridge();
        bridge.operate(new String[] {"mock", "arg1", "arg2"});
        Assert.assertEquals("first param is: arg1, second param is: arg2", myOut.toString());
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
//
//    @Test
//    public void testProgress() {
//        Progress p = new Progress(1000);
//        p.show();
//        Assert.assertEquals("0% [0/1000]", myOut.toString());
//
//        p.progress(100);
//        Assert.assertEquals("10% [100/1000]", myOut.toString());
//
//        p.progress(500);
//        Assert.assertEquals("50% [500/1000]", myOut.toString());
//
//        p.progress(999);
//        Assert.assertEquals("0% [0/1000]", myOut.toString());
//
//        p.progress(1000);
//        Assert.assertEquals("", myOut.toString());
//    }
}
