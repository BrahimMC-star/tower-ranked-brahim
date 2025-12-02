package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParameter;
import zwuiix.colria.util.Random;

import java.util.Locale;
import java.util.Set;

public class BoolArgument extends CommandArgument {
    private static final Set<String> TRUE = Set.of(
            "true", "t", "1", "yes", "y", "on", "enable", "enabled"
    );
    private static final Set<String> FALSE = Set.of(
            "false", "f", "0", "no", "n", "off", "disable", "disabled"
    );

    public BoolArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public Boolean parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (TRUE.contains(s)) return true;
        if (FALSE.contains(s)) return false;
        throw new IllegalArgumentException("Expected boolean value (true/false, yes/no, on/off). Got: '" + raw + "'");
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newEnum(getName(), new CommandEnum("enum#" + Random.utf8(4).toUpperCase(), "true", "false", "yes", "no", "on", "enable", "enabled"));
    }
}
