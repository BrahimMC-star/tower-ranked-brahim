package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import zwuiix.colria.util.Regex;

import java.security.InvalidParameterException;

public class FloatArgument extends CommandArgument{
    public FloatArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public Float parse(String raw) {
        if(!Regex.isDecimal(raw)) {
            throw new InvalidParameterException("Expected an float, got: " + raw);
        }

        return Float.parseFloat(raw.trim());
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newType(getName(), isOptional(), CommandParamType.FLOAT);
    }
}
