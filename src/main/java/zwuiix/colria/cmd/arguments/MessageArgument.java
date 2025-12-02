package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class MessageArgument extends CommandArgument{
    public MessageArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public String parse(String raw) {
        return raw;
    }

    @Override
    public int getSpanLength() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newType(getName(), isOptional(), CommandParamType.TEXT);
    }
}
