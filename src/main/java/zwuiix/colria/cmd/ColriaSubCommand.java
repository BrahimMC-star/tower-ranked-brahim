package zwuiix.colria.cmd;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.arguments.CommandArgument;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static zwuiix.colria.cmd.ColriaCommand.buildUsageFor;

abstract public class ColriaSubCommand {
    private String name;
    private String permission = null;

    private final HashMap<Integer, CommandArgument> arguments = new HashMap<>();

    public ColriaSubCommand(String name) {
        this.name = name;
        prepare();
    }

    public ColriaSubCommand(String name, String permission) {
        this.name = name;
        this.permission = permission;

        prepare();
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(CommandSender sender) {
        if(permission == null) return true;
        return sender.hasPermission(permission);
    }

    public boolean hasConditions(CommandSender sender) {
        return false;
    }

    public HashMap<Integer, CommandArgument> getArguments() {
        return arguments;
    }

    public void registerArgument(Integer position, CommandArgument arg) {
        if(arguments.containsKey(position)) {
            throw new InvalidParameterException("Argument " + position + " is already registered");
        }

        arguments.put(position, arg);
    }

    public void sendUsage(CommandSender sender, String commandLabel) {
        sender.sendMessage("§7Usage: §b/" + commandLabel + " " + buildUsageFor(arguments));
    }

    public void prepare() {}
    abstract public void run(CommandSender sender, Map<String, Object> args);
}
