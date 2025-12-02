package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class StringArgument extends CommandArgument{
    public StringArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public String parse(String raw) {
        return raw;
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newType(getName(), isOptional(), CommandParamType.RAWTEXT);
    }
}
