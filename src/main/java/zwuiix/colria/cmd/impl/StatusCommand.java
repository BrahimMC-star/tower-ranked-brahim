package zwuiix.colria.cmd.impl;

import cn.nukkit.Nukkit;
import cn.nukkit.command.CommandSender;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Glyph;

import java.util.Comparator;
import java.util.Map;

public class StatusCommand extends ColriaCommand {
    private final SystemInfo systemInfo = new SystemInfo();

    public StatusCommand() {
        super("status", "%nukkit.command.status.description");
        setAliases(new String[]{"tps"});
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        Translator translator = Translator.getInstance();

        long time = System.currentTimeMillis() - Nukkit.START_TIME;
        String protocolsRange = ProtocolInfo.SUPPORTED_PROTOCOLS.stream()
                .min(Comparator.naturalOrder())
                .map(min -> {
                    int max = ProtocolInfo.SUPPORTED_PROTOCOLS.stream()
                            .max(Comparator.naturalOrder())
                            .orElse(min);
                    return Utils.getVersionByProtocol(min) + " - " + Utils.getVersionByProtocol(max);
                })
                .orElse("n/a");

        TextFormat tpsColor = TextFormat.GREEN;
        float tps = sender.getServer().getTicksPerSecond();
        if (tps < 12) {
            tpsColor = TextFormat.RED;
        } else if (tps < 17) {
            tpsColor = EngineInfo.COLOR;
        }

        Network network = sender.getServer().getNetwork();
        CentralProcessor cpu = systemInfo.getHardware().getProcessor();

        GlobalMemory globalMemory = systemInfo.getHardware().getMemory();
        long allPhysicalMemory = globalMemory.getTotal() / 1000;
        long usedPhysicalMemory = (globalMemory.getTotal() - globalMemory.getAvailable()) / 1000;

        double usage = (double) usedPhysicalMemory / allPhysicalMemory * 100;

        sender.sendMessage(Glyph.hbarThick(TextFormat.DARK_GRAY, 2));
        sender.sendMessage(Glyph.translate(translator.autoProcess(sender, TranslationKeys.STATUS_TITLE), EngineInfo.COLOR));
        sender.sendMessage(" ");
        sender.sendMessage(translator.autoProcess(sender, TranslationKeys.STATUS_DETAILS,
                TextFormat.clean(cn.nukkit.command.defaults.StatusCommand.formatUptime(time)),
                EngineInfo.VERSION,
                protocolsRange,
                tpsColor.toString() + NukkitMath.round(tps, 2),
                tpsColor.toString() + sender.getServer().getTickUsage() + "%",
                TextFormat.GREEN + formatKB(network.getUpload()) + "/s",
                TextFormat.GREEN + formatKB(network.getDownload()) + "/s",
                cpu.getProcessorIdentifier().getName(),
                Thread.getAllStackTraces().size(),
                formatMB(usedPhysicalMemory) + " / " + formatMB(allPhysicalMemory) + ". (" + NukkitMath.round(usage, 2) + "%)"
        ).replace("{VBAR}", Glyph.vbar(TextFormat.DARK_GRAY, 1)));
        sender.sendMessage(Glyph.hbarThick(TextFormat.DARK_GRAY, 2));
    }

    private static String formatKB(double bytes) {
        return NukkitMath.round((bytes / 1024 * 1000), 2) + " KB";
    }

    private static String formatKB(long bytes) {
        return NukkitMath.round((bytes / 1024d * 1000), 2) + " KB";
    }

    private static String formatMB(double bytes) {
        return NukkitMath.round((bytes / 1024 / 1024 * 1000), 2) + " MB";
    }

    private static String formatFreq(long hz) {
        if (hz >= 1000000000) {
            return String.format("%.2fGHz", hz / 1000000000.0);
        }
        if (hz >= 1000 * 1000) {
            return String.format("%.2fMHz", hz / 1000000.0);
        }
        if (hz >= 1000) {
            return String.format("%.2fKHz", hz / 1000.0);
        }
        return String.format("%dHz", hz);
    }
}
