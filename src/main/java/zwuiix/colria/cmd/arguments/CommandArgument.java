package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParameter;

abstract public class CommandArgument {
    private final String name;
    private final boolean optional;

    public CommandArgument(String name, boolean optional) {
        this.name = name.toLowerCase();
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public int getSpanLength() {
        return 1;
    }

    abstract public Object parse(String raw);

    abstract public CommandParameter getParameter();
}
