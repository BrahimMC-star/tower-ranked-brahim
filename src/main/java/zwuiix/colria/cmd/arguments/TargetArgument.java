package zwuiix.colria.cmd.arguments;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import zwuiix.colria.util.Regex;

import java.security.InvalidParameterException;

public class TargetArgument extends CommandArgument{
    public TargetArgument(String name, boolean optional) {
        super(name, optional);
    }

    @Override
    public String parse(String raw) {
        if(!Regex.isBedrockUsername(raw)) {
            throw new InvalidParameterException("Expected an target, got: " + raw);
        }

        return raw;
    }

    @Override
    public CommandParameter getParameter() {
        return CommandParameter.newType(getName(), isOptional(), CommandParamType.TARGET);
    }
}
