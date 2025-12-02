package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import zwuiix.colria.util.Regex;

import java.security.InvalidParameterException;

public class LongArgument extends CommandArgument{
    public LongArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public Long parse(String raw) {
        if(!Regex.isInt(raw)) {
            throw new InvalidParameterException("Expected an integer, got: " + raw);
        }

        return Long.parseLong(raw.trim());
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newType(getName(), isOptional(), CommandParamType.INT);
    }
}
