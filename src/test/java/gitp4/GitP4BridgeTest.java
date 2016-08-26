package gitp4;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

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

}
