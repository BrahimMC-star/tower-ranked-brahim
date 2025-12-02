package zwuiix.colria.cmd;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.*;
import cn.nukkit.lang.PluginI18nManager;
import cn.nukkit.plugin.InternalPlugin;
import cn.nukkit.plugin.PluginBase;
import zwuiix.colria.cmd.arguments.CommandArgument;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Random;

import java.security.InvalidParameterException;
import java.util.*;

abstract public class ColriaCommand extends Command {
    private final HashMap<String, ColriaSubCommand> subCommands = new HashMap<>();
    private final HashMap<Integer, CommandArgument> arguments = new HashMap<>();

    public ColriaCommand(String name, String description) {
        super(name, description);

        prepare();
    }

    @Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        if (!this.testPermission(player)) return null;

        if(subCommands.isEmpty() && arguments.isEmpty()) {
            return super.generateCustomCommandData(player);
        }

        CommandData customData = new CommandData();
        customData.description = player.getServer().getLanguage().translateString(this.getDescription());

        if (getAliases().length > 0) {
            List<String> aliases = new ArrayList<>(Arrays.asList(getAliases()));
            if (!aliases.contains(this.getName())) {
                aliases.add(this.getName());
            }

            customData.aliases = new CommandEnum(this.getName() + "Aliases", aliases);
        }

        if(!subCommands.isEmpty()) {
            for (ColriaSubCommand entry : subCommands.values()) {
                if(!entry.hasPermission(player) && !entry.hasConditions(player)) continue;

                ArrayList<CommandParameter> args = new ArrayList<>();
                args.add(CommandParameter.newEnum("enum#" + Random.utf8(4).toUpperCase(), false, new String[]{entry.getName()}));

                for (CommandArgument argument : entry.getArguments().values()) {
                    args.add(argument.getParameter());
                }

                CommandOverload overload = new CommandOverload();
                overload.input.parameters = args.toArray(CommandParameter[]::new);
                customData.overloads.put(entry.getName(), overload);
            }
        }

        if(!arguments.isEmpty()) {
            CommandOverload overload = new CommandOverload();
            overload.input.parameters = arguments.values().stream().map(CommandArgument::getParameter).toArray(CommandParameter[]::new);
            customData.overloads.put("arguments", overload);
        }

        CommandDataVersions versions = new CommandDataVersions();
        versions.versions.add(customData);
        return versions;
    }

    public HashMap<String, ColriaSubCommand> getSubCommands() {
        return subCommands;
    }

    public HashMap<Integer, CommandArgument> getArguments() {
        return arguments;
    }

    public void registerSubCommand(ColriaSubCommand command) {
        subCommands.put(command.getName(), command);
    }

    public void registerArgument(Integer position, CommandArgument arg) {
        if(arguments.containsKey(position)) {
            throw new InvalidParameterException("Argument " + position + " is already registered");
        }

        arguments.put(position, arg);
    }

    public void prepare() {}
    abstract public void run(CommandSender sender, Map<String, Object> args);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_NOPERM));
            return true;
        }

        if (!subCommands.isEmpty() && args.length > 0) {
            String subName = args[0];
            ColriaSubCommand sub = subCommands.get(subName);
            if (sub != null) {
                if(getPermission() != null && !sub.hasPermission(sender) && !sub.hasConditions(sender)) {
                    sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_NOPERM));
                    return true;
                }

                String[] tail = Arrays.copyOfRange(args, 1, args.length);

                try {
                    Map<String, Object> parsed = parseArgsOrThrow(sub.getArguments(), tail);
                    sub.run(sender, parsed);
                } catch (IllegalArgumentException ex) {
                    //sender.sendMessage("§c" + ex.getMessage());
                    sub.sendUsage(sender, commandLabel + " " + subName);
                }
                return true;
            }
        }

        try {
            Map<String, Object> parsed = parseArgsOrThrow(arguments, args);
            run(sender, parsed);
        } catch (IllegalArgumentException ex) {
            //sender.sendMessage("§c" + ex.getMessage());
            sendUsage(sender, commandLabel);
        }
        return true;
    }

    public void sendUsage(CommandSender sender, String commandLabel) {
        sender.sendMessage("§7Usage: §b/" + commandLabel + " " + buildUsageFor(arguments));
    }

    public static Map<String, Object> parseArgsOrThrow(Map<Integer, CommandArgument> defs, String[] tokens)
            throws IllegalArgumentException {

        Map<String, Object> out = new HashMap<>();
        if (defs == null || defs.isEmpty()) {
            if (tokens.length > 0) throw new IllegalArgumentException("Too many arguments.");
            return out;
        }

        var ordered = new java.util.TreeMap<>(defs);
        int pos = 0;
        int len = tokens.length;

        for (var entry : ordered.entrySet()) {
            int idx = entry.getKey();
            CommandArgument def = entry.getValue();
            if (def == null) continue;

            if (pos >= len) {
                if (def.isOptional()) continue;
                throw new IllegalArgumentException("Missing required argument: " + def.getName() + " (position " + idx + ").");
            }

            int span = def.getSpanLength();
            if (span <= 0) span = 1;

            String raw;
            if (span == Integer.MAX_VALUE) {
                raw = joinTokens(tokens, pos, len);
                pos = len;
            } else if (pos + span <= len) {
                raw = joinTokens(tokens, pos, pos + span);
                pos += span;
            } else {
                if (def.isOptional()) {
                    continue;
                }
                throw new IllegalArgumentException("Missing required argument: " + def.getName() + " (position " + idx + ").");
            }

            Object parsed;
            try {
                parsed = def.parse(raw);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value for '" + def.getName() + "': " + raw);
            }
            out.put(def.getName(), parsed);
        }

        if (pos < len) {
            throw new IllegalArgumentException("Too many arguments.");
        }
        return out;
    }

    private static String joinTokens(String[] arr, int fromInclusive, int toExclusive) {
        if (fromInclusive >= toExclusive) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (i > fromInclusive) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    public static String buildUsageFor(Map<Integer, CommandArgument> defs) {
        if (defs == null || defs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        defs.keySet().stream().sorted().forEach(i -> {
            CommandArgument def = defs.get(i);
            if (def == null) return;
            boolean optional = def.isOptional();
            boolean varSpan  = def.getSpanLength() == Integer.MAX_VALUE || def.getSpanLength() > 1;
            String name = def.getName() + (varSpan ? "..." : "");
            if (optional) sb.append("[").append(name).append("] ");
            else          sb.append("<").append(name).append("> ");
        });
        return sb.toString().trim();
    }
}