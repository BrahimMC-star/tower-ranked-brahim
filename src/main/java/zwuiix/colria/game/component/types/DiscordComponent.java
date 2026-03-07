package zwuiix.colria.game.component.types;

import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import zwuiix.colria.game.component.GameComponent;
import zwuiix.colria.game.impl.team.Team;

import java.util.Map;

public class DiscordComponent implements GameComponent {
    private VoiceChannel lobbyChannel;
    private Map<Team, VoiceChannel> teamChannels;

    private boolean spectatorsCanTalk = false;
    private boolean playersCanTalk = true;
}
