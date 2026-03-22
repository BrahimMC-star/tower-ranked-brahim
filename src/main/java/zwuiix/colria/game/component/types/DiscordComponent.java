package zwuiix.colria.game.component.types;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import zwuiix.colria.discord.DiscordUtil;
import zwuiix.colria.game.component.GameComponent;
import zwuiix.colria.game.impl.team.Team;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.Translator;

import java.util.HashMap;
import java.util.Map;

public class DiscordComponent extends GameComponent {

    private final Guild guild;

    public Category category;

    public VoiceChannel lobbyChannel;
    public VoiceChannel spectatorChannel;

    public Map<Team, VoiceChannel> teamChannels = new HashMap<>();

    public boolean spectatorsCanTalk = false;
    public boolean playersCanTalk = true;

    public DiscordComponent(TeamGame game) {
        super(game);

        this.guild = DiscordUtil.getGuild().orElseThrow();

        guild.createCategory("→ " + game.getGameId()).queue(createdCategory -> {
            this.category = createdCategory;

            createdCategory.upsertPermissionOverride(guild.getPublicRole())
                    .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                    .queue();

            createdCategory.getManager()
                    .setPosition(guild.getCategories().size() - 1)
                    .queue(success -> createLobbyChannel());
        });
    }

    private void createLobbyChannel() {
        category.createVoiceChannel("→ Attente").queue(channel -> {
            this.lobbyChannel = channel;
            channel.upsertPermissionOverride(guild.getPublicRole())
                    .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                    .queue();

            channel.getManager()
                    .setUserLimit(99)
                    .queue();

            game.getSpectators().forEach((id, spectator) -> onPlayerJoin(spectator.getPlayerDataInfo().getDiscordId()));
        });
    }

    public void onPlayerJoin(String userId) {
        if (category == null) return;

        Member member = guild.getMemberById(userId);
        if (member == null) return;

        category.upsertPermissionOverride(member)
                .grant(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                .queue();

        if (!playersCanTalk) {
            category.upsertPermissionOverride(member)
                    .deny(Permission.VOICE_SPEAK)
                    .queue();
        }

        if (lobbyChannel != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
            guild.moveVoiceMember(member, lobbyChannel).queue();
        }
    }

    public void onPlayerQuit(String userId) {
        if (category == null) return;

        Member member = guild.getMemberById(userId);
        if (member == null) return;

        var override = category.getPermissionOverride(member);
        if (override != null) {
            override.delete().queue();
        }
    }

    public void onGameStart() {
        TeamGame game = (TeamGame) this.game;
        if (lobbyChannel == null || category == null) return;

        lobbyChannel.getManager()
                .setName("\uD83C\uDF10┃Spectateurs")
                .queue(success -> spectatorChannel = lobbyChannel);

        Team teamA = game.getTeamA();
        Team teamB = game.getTeamB();

        var emojiA = switch (teamA.dyeColor()) {
            case RED -> "🔴";
            case BLUE -> "🔵";
            case CYAN -> "\uD83E\uDE75";
            case LIGHT_BLUE -> "\uD83D\uDC99";
            case GREEN -> "🟢";
            case LIME -> "\uD83D\uDC9A";
            case YELLOW -> "🟡";
            case PURPLE -> "🟣";
            case MAGENTA -> "\uD83D\uDC9C";
            case PINK -> "\uD83E\uDE77";
            case ORANGE -> "🟠";
            case BLACK -> "⚫";
            case LIGHT_GRAY -> "\uD83E\uDE76";
            case GRAY -> "\uD83D\uDDA4";
            case BROWN -> "\uD83E\uDD0E";
            default -> "⚪";
        };

        var emojiB = switch (teamB.dyeColor()) {
            case RED -> "🔴";
            case BLUE -> "🔵";
            case CYAN -> "\uD83E\uDE75";
            case LIGHT_BLUE -> "\uD83D\uDC99";
            case GREEN -> "🟢";
            case LIME -> "\uD83D\uDC9A";
            case YELLOW -> "🟡";
            case PURPLE -> "🟣";
            case MAGENTA -> "\uD83D\uDC9C";
            case PINK -> "\uD83E\uDE77";
            case ORANGE -> "🟠";
            case BLACK -> "⚫";
            case LIGHT_GRAY -> "\uD83E\uDE76";
            case GRAY -> "\uD83D\uDDA4";
            case BROWN -> "\uD83E\uDD0E";
            default -> "⚪";
        };

        category.createVoiceChannel(emojiA + "┃" + Translator.getInstance().autoProcess(null, teamA.name()))
                .queue(channel -> {
                    channel.upsertPermissionOverride(guild.getPublicRole())
                            .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                            .queue();

                    teamChannels.put(teamA, channel);

                    channel.getManager()
                            .setUserLimit(99)
                            .queue();

                    applyTeamPermissions(channel, teamA);
                    moveTeamPlayers(teamA, channel);
                });

        category.createVoiceChannel(emojiB + "┃" + Translator.getInstance().autoProcess(null, teamB.name()))
                .queue(channel -> {
                    channel.upsertPermissionOverride(guild.getPublicRole())
                            .deny(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                            .queue();

                    teamChannels.put(teamB, channel);

                    channel.getManager()
                            .setUserLimit(99)
                            .queue();

                    applyTeamPermissions(channel, teamB);
                    moveTeamPlayers(teamB, channel);

                });

        applySpectatorPermissions();
    }

    private void applyTeamPermissions(VoiceChannel channel, Team team) {
        TeamGame game = (TeamGame) this.game;

        game.getTeams().forEach((nukkitPlayer, playerTeam) -> {

            var info = nukkitPlayer.getPlayerDataInfo();
            String discordId = info.getDiscordId();

            if (discordId.isEmpty()) return;

            Member member = guild.getMemberById(discordId);
            if (member == null) return;

            if (playerTeam == team) {

                channel.upsertPermissionOverride(member)
                        .grant(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                        .queue();

                if (!playersCanTalk) {
                    channel.upsertPermissionOverride(member)
                            .deny(Permission.VOICE_SPEAK)
                            .queue();
                }

            } else {

                channel.upsertPermissionOverride(member)
                        .deny(Permission.VOICE_CONNECT)
                        .queue();

            }

        });
    }

    private void moveTeamPlayers(Team team, VoiceChannel channel) {

        TeamGame game = (TeamGame) this.game;

        game.getTeams().forEach((nukkitPlayer, playerTeam) -> {

            if (playerTeam != team) return;

            var info = nukkitPlayer.getPlayerDataInfo();
            String discordId = info.getDiscordId();

            if (discordId.isEmpty()) return;

            Member member = guild.getMemberById(discordId);
            if (member == null) return;

            if (member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
                guild.moveVoiceMember(member, channel).queue();
            }

        });
    }

    private void applySpectatorPermissions() {
        if (spectatorChannel == null) return;

        TeamGame game = (TeamGame) this.game;

        for (EnginePlayer spectator : game.getSpectators().values()) {
            String discordId = spectator.getPlayerDataInfo().getDiscordId();
            if (discordId.isEmpty()) continue;

            Member member = guild.getMemberById(discordId);
            if (member == null) continue;

            spectatorChannel.upsertPermissionOverride(member)
                    .grant(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)
                    .queue();

            if (!spectatorsCanTalk) {
                spectatorChannel.upsertPermissionOverride(member)
                        .deny(Permission.VOICE_SPEAK)
                        .queue();
            }
        }
    }

    public void onGameEnd() {
        if (category == null) return;

        category.getChannels().forEach(channel ->
                channel.delete().queue(
                        success -> {},
                        error -> {}
                )
        );

        category.delete().queue(
                success -> {},
                error -> {}
        );
    }
}