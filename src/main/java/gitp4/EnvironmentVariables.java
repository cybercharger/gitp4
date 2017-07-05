package gitp4;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ChrisKang on 7/5/2017.
 */
public enum EnvironmentVariables {
    LogDir("log.dir");

    private final String variableName;

    EnvironmentVariables(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public static String verify() {
        List<String> missingVariables = EnumSet.allOf(EnvironmentVariables.class).stream()
                .filter(s -> System.getProperty(s.variableName) == null)
                .map(s -> s.variableName)
                .collect(Collectors.toList());
        if (missingVariables.isEmpty()) return null;
        return missingVariables.isEmpty() ?
                null :
                String.format("Missing Environment Variable(s): %s", StringUtils.join(missingVariables, ", "));
    }
}
