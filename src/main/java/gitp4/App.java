package gitp4;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        Map<String, Method> methodMap = new HashMap<>();
        for(Method m : GitP4Bridge.class.getMethods()) {
            GitP4Operation gpo = m.getAnnotation(GitP4Operation.class);
            if (gpo == null) continue;
            methodMap.put(m.getName(), m);
        }

        if (args.length != 2 || !methodMap.containsKey(args[0])) {
            logError(methodMap.keySet());
            return;
        }

        GitP4Bridge bridge = new GitP4Bridge();

        methodMap.get(args[0]).invoke(bridge, args[1]);
    }

    private static void logError(Set<String> operations) {
        logger.error("Please provide operation and proper parameters");
        logger.error(String.format("Valid operations are: %s", StringUtils.join(operations, " ")));
    }
}
