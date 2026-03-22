package zwuiix.colria.cmd.impl.mute;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.arguments.StringArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.punishment.mute.MuteEntry;
import zwuiix.colria.punishment.mute.MuteManager;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand extends ColriaCommand {

    public MuteCommand() {
        super("mute", "Mute a player");
        setPermission(Permission.STAFF_MUTE.toString());

        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new StringArgument("duration", true));
        registerArgument(2, new StringArgument("reason", true));
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        var manager = MuteManager.getInstance();
        String targetName = args.get("target").toString();
        String reason = args.getOrDefault("reason", "No reason provided").toString();

        if (manager.isMuted(targetName)) {
            sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_MUTE_ALREADY, targetName));
            return;
        }

        long now = System.currentTimeMillis();
        long expiresAt = -1;

        if (args.containsKey("duration")) {
            String durationArg = (String) args.get("duration");
            if (durationArg.equalsIgnoreCase("perm")) {
                expiresAt = -1;
            } else {
                try {
                    long duration = parseDuration(durationArg);
                    expiresAt = now + duration;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_MUTE_INVALID_DURATION, durationArg));
                    return;
                }
            }
        }

        MuteEntry entry = new MuteEntry(targetName, reason, sender.getName(), now, expiresAt);
        manager.mute(entry);

        sender.sendMessage(Translator.getInstance().autoProcess(sender, TranslationKeys.PLAYER_COMMAND_MUTE_SUCCESS, targetName, reason));
        Chat.broadcast(TranslationKeys.PLAYER_COMMAND_MUTE_BROADCAST, targetName, sender.getName(), reason);
    }

    private long parseDuration(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(input);

        long total = 0;

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
                case "d": total += value * 86400000L; break;
                case "h": total += value * 3600000L; break;
                case "m": total += value * 60000L; break;
                case "s": total += value * 1000L; break;
            }
        }

        if (total == 0) throw new IllegalArgumentException();
        return total;
    }
}