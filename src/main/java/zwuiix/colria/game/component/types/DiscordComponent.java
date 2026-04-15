package zwuiix.colria.game.component.types;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import zwuiix.colria.discord.DiscordUtil;
import zwuiix.colria.game.component.GameComponent;
import zwuiix.colria.game.impl.team.*;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.Translator;

import java.util.*;

public class DiscordComponent extends GameComponent {

    private final Guild guild;

    public Category category;
    public VoiceChannel lobbyChannel, spectatorChannel;
    public Map<Team, VoiceChannel> teamChannels = new HashMap<>();

    public boolean spectatorsCanTalk = false;
    public boolean playersCanTalk = true;

    public DiscordComponent(TeamGame game) {
        super(game);
        this.guild = DiscordUtil.getGuild().orElseThrow();

        guild.createCategory("→ " + game.getGameId()).queue(cat -> {
            category = cat;

            cat.upsertPermissionOverride(guild.getPublicRole())
                    .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                    .queue();

            cat.getManager().setPosition(guild.getCategories().size() - 1)
                    .queue(v -> createLobbyChannel());
        });
    }

    private void createLobbyChannel() {
        category.createVoiceChannel("→ Attente").queue(vc -> {
            lobbyChannel = vc;

            vc.upsertPermissionOverride(guild.getPublicRole())
                    .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                    .queue();

            vc.getManager().setUserLimit(99).queue();

            ((TeamGame) game).getSpectators().values()
                    .forEach(p -> onPlayerJoin(p.getPlayerDataInfo().getDiscordId()));
        });
    }

    private void clear(Member m) {
        category.getChannels().forEach(c -> {
            var o = c.getPermissionContainer().getPermissionOverride(m);
            if (o != null) o.delete().queue();
        });
    }

    private void allow(VoiceChannel c, Member m, boolean speak) {
        c.upsertPermissionOverride(m)
                .grant(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                .queue(
                        s -> {},
                        Throwable::printStackTrace
                );

        if (!speak) {
            c.upsertPermissionOverride(m)
                    .deny(Permission.VOICE_SPEAK)
                    .queue(null, Throwable::printStackTrace);
        }
    }

    private void move(Member m, VoiceChannel c) {
        if (m.getVoiceState() != null && m.getVoiceState().inAudioChannel()) {
            guild.moveVoiceMember(m, c).queue(null, Throwable::printStackTrace);
        }
    }

    private void withMember(String id, java.util.function.Consumer<Member> consumer) {
        guild.retrieveMemberById(id).queue(
                consumer,
                err -> System.out.println("Failed to fetch member: " + id)
        );
    }

    public void onPlayerJoin(String id) {
        if (category == null) return;

        withMember(id, m -> {
            clear(m);

            if (lobbyChannel != null) {
                allow(lobbyChannel, m, playersCanTalk);
                move(m, lobbyChannel);
            }
        });
    }

    public void onPlayerQuit(String id) {
        if (category == null) return;

        withMember(id, this::clear);
    }

    public void onGameStart() {
        TeamGame g = (TeamGame) game;
        if (category == null || lobbyChannel == null) return;

        lobbyChannel.getManager()
                .setName("\uD83C\uDF10┃Spectateurs")
                .queue(v -> spectatorChannel = lobbyChannel);

        clearAll();

        createTeam(g.getTeamA());
        createTeam(g.getTeamB());

        applySpectators();
    }

    private void clearAll() {
        TeamGame g = (TeamGame) game;

        g.getTeams().forEach((p, t) -> {
            String id = p.getPlayerDataInfo().getDiscordId();
            if (!id.isEmpty()) withMember(id, this::clear);
        });

        g.getSpectators().values().forEach(sp -> {
            String id = sp.getPlayerDataInfo().getDiscordId();
            if (!id.isEmpty()) withMember(id, this::clear);
        });
    }

    private void createTeam(Team team) {
        category.createVoiceChannel(getEmoji(team) + "┃" +
                        Translator.getInstance().autoProcess(null, team.name()))
                .queue(vc -> {

                    vc.upsertPermissionOverride(guild.getPublicRole())
                            .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                            .queue();

                    vc.getManager().setUserLimit(99).queue();
                    teamChannels.put(team, vc);

                    applyTeam(team, vc);
                });
    }

    private void applyTeam(Team team, VoiceChannel vc) {
        TeamGame g = (TeamGame) game;

        g.getTeams().forEach((p, t) -> {
            if (t != team) return;

            String id = p.getPlayerDataInfo().getDiscordId();
            if (id.isEmpty()) return;

            withMember(id, m -> {
                allow(vc, m, playersCanTalk);
                move(m, vc);
            });
        });
    }

    private void applySpectators() {
        if (spectatorChannel == null) return;

        TeamGame g = (TeamGame) game;

        for (EnginePlayer sp : g.getSpectators().values()) {
            String id = sp.getPlayerDataInfo().getDiscordId();
            if (id.isEmpty()) continue;

            withMember(id, m -> {
                allow(spectatorChannel, m, spectatorsCanTalk);
                move(m, spectatorChannel);
            });
        }
    }

    public void onGameEnd() {
        if (category == null) return;

        category.getChannels().forEach(c ->
                c.delete().queue(null, Throwable::printStackTrace)
        );

        category.delete().queue(null, Throwable::printStackTrace);
    }

    private String getEmoji(Team t) {
        return switch (t.dyeColor()) {
            case RED -> "🔴";
            case BLUE -> "🔵";
            case CYAN -> "🔷";
            case LIGHT_BLUE -> "💎";
            case GREEN -> "🟢";
            case LIME -> "🍏";
            case YELLOW -> "🟡";
            case PURPLE -> "🟣";
            case MAGENTA -> "💜";
            case PINK -> "🌸";
            case ORANGE -> "🟠";
            case BLACK -> "⚫";
            case GRAY -> "⬜";
            case BROWN -> "🟤";
            default -> "⚪";
        };
    }
}