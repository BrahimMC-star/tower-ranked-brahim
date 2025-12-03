package zwuiix.colria.discord.cmd.impl;

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.database.dao.PlayerDataDao;
import zwuiix.colria.database.dao.PlayerParticleDao;
import zwuiix.colria.database.dao.PlayerRankDao;
import zwuiix.colria.discord.cmd.DiscordCommand;
import zwuiix.colria.player.PlayerDataInfo;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.Translator;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfilCommand extends DiscordCommand {
    public ProfilCommand() { super("profil"); }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash(name,  "Get info about a user's account.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Info sur le compte d'un utilisateur.")
                .addOption(OptionType.USER, "user", "The user to get info about.", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent ev) {
        ev.deferReply().queue();

        var userOption = ev.getOption("user");
        var userId = userOption != null ? userOption.getAsUser().getId() : ev.getUser().getId();

        ev.getHook().editOriginal("Recherche des informations du compte, veuillez patienter...").queue();

        long start = System.currentTimeMillis();
        var db = DataBase.getInstance();

        db.write(PlayerDataDao.class, (Function<PlayerDataDao, PlayerDataInfo>) dao -> dao.getFromDiscordId(userId))
                .thenCompose(info -> {
                    if(info == null) {
                        throw new NoSuchElementException();
                    }

                    var xuid = info.getXuid();
                    var ranks = db.query(PlayerRankDao.class, dao -> dao.listByXuid(xuid));
                    var particles = db.query(PlayerParticleDao.class, dao -> dao.listByXuid(xuid));
                    var cosmetics = db.query(PlayerCosmeticDao.class, dao -> dao.listByXuid(xuid));

                    return CompletableFuture.allOf(ranks, particles, cosmetics)
                            .thenApply(v -> new Information(
                                    info,
                                    ranks.join(),
                                    particles.join(),
                                    cosmetics.join()
                            ));
                })
                .thenAccept(info -> retrieve(ev, info, System.currentTimeMillis() - start))
                .exceptionally(ex -> {
                    if(userOption == null) {
                        ev.getHook().editOriginal("Vous n'avez pas de compte lié.").queue();
                        return null;
                    }

                    ev.getHook().editOriginal("Aucun compte lié trouvé pour cet utilisateur.").queue();
                    return null;
                });
    }

    private void retrieve(SlashCommandInteractionEvent ev, Information info, long elapsed) {
        var player = info.player();
        var ranks = info.ranks();
        var particles = info.particles();
        var translator = Translator.getInstance();
        var consoleSender = Server.getInstance().getConsoleSender();

        var cosmetics = info.cosmetics().stream()
                .filter(cosmetic -> CosmeticRegistry.getInstance().getCosmetic(cosmetic) != null)
                .toList();

        var capes = info.cosmetics().stream()
                .filter(cosmetic -> CosmeticRegistry.getInstance().getCape(cosmetic) != null)
                .toList();

        var capesFormat = capes.isEmpty()
                ? "aucune"
                : capes.stream()
                .map(v -> TextFormat.clean(translator.autoProcess(consoleSender, CosmeticRegistry.getInstance().getCape(v).getName())))
                .collect(Collectors.joining(", "));

        var cosmeticFormat = cosmetics.isEmpty()
                ? "aucun"
                : cosmetics.stream()
                .filter(cosmetic -> CosmeticRegistry.getInstance().getCosmetic(cosmetic) != null)
                .map(v -> TextFormat.clean(translator.autoProcess(consoleSender, CosmeticRegistry.getInstance().getCosmetic(v).getName())))
                .collect(Collectors.joining(", "));

        var particlesFormat = particles.isEmpty()
                ? "aucune"
                : particles.stream()
                .map(v -> TextFormat.clean(translator.autoProcess(consoleSender, ParticleRegistry.getInstance().getParticle(v).getName())))
                .collect(Collectors.joining(", "));

        var vbar = TextDisplay.of("<:horizontal_bar_dark_aqua:1443280871292534835>".repeat(6));
        var hbar = "<:vertical_bar_dark_aqua:1443277514926592020>";

        var date = new Date(info.player().getLastLogin());
        var format = String.format("%d/%d/%d à %dh%d",
                date.getDay(),
                date.getMonth(),
                date.getYear(),
                date.getHours(),
                date.getMinutes()
        );

        long totalSeconds = info.player().getPlaytime() / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        StringBuilder playtimeBuilder = new StringBuilder();

        if (days > 0) playtimeBuilder.append(days).append("j ");
        if (hours > 0) playtimeBuilder.append(hours).append("h ");
        if (minutes > 0) playtimeBuilder.append(minutes).append("m");
        String playtimeFormatted = playtimeBuilder.toString().trim();

        var bestRank = info.ranks().stream()
                .map(rankId -> RankRegistry.getInstance().getRank(rankId))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(rank -> rank.orElseThrow().getId()))
                .orElse(Optional.empty());
        if(bestRank.isEmpty()) bestRank = Optional.of(RankRegistry.getInstance().getDefaultRank());

        var container = Container.of(
                TextDisplay.of("# Profile de **" + player.getName() + "**"),
                TextDisplay.of("**Informations Générales**"),
                TextDisplay.of(hbar + "Rôle : " + bestRank.orElseThrow().getName()),
                TextDisplay.of(hbar + "Éclats : " + player.getShards()),
                TextDisplay.of(hbar + "Boosters : " + player.getBooster()),
                TextDisplay.of(hbar + "Dernière connexion : " + format),
                TextDisplay.of(hbar + "Temps de jeu : " + playtimeFormatted),
                Separator.createInvisible(Separator.Spacing.SMALL),
                TextDisplay.of("**Cosmétiques & Particules**"),
                TextDisplay.of(hbar + "Particules (" + particles.size() + ") : " + particlesFormat),
                TextDisplay.of(hbar + "Capes (" + capes.size() + ") : " + capesFormat),
                TextDisplay.of(hbar + "Cosmétiques (" + cosmetics.size() + ") : " + cosmeticFormat),
                Separator.createInvisible(Separator.Spacing.SMALL),
                TextDisplay.of("**Statistiques en Tower**"),
                TextDisplay.of("_Requête traitée en " + elapsed + " ms._")
        );

        ev.getHook().editOriginalComponents(container).setReplace(true).useComponentsV2().queue();
    }

    private record Information(
            PlayerDataInfo player,
            List<Integer> ranks,
            List<String> particles,
            List<String> cosmetics
    ) {}
}
