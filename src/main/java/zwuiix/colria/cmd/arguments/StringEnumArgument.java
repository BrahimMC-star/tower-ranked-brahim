package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParameter;
import zwuiix.colria.util.Random;

import java.util.Arrays;

public class StringEnumArgument extends CommandArgument {
    private final String[] values;

    public StringEnumArgument(String name, boolean optional, String... values)
    {
        super(name, optional);
        this.values = values;
    }

    @Override
    public String parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        final String input = raw.trim();
        final String alternation = java.util.Arrays.stream(values)
                .map(java.util.regex.Pattern::quote)
                .collect(java.util.stream.Collectors.joining("|"));

        @SuppressWarnings("regex")
        final String regex = "^(?:" + alternation + ")$";

        final java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                regex,
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE
        );

        if (!p.matcher(input).matches()) {
            throw new java.security.InvalidParameterException(
                    "Invalid value for '" + getName() + "': '" + raw +
                            "'. Expected one of: " + String.join(", ", values)
            );
        }

        for (String v : values) {
            if (v.equalsIgnoreCase(input)) {
                return v;
            }
        }

        return input;
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newEnum(getName(), new CommandEnum("enum#" + Random.utf8(4).toUpperCase(), Arrays.asList(values)));
    }
}