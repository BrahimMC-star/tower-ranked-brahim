package zwuiix.colria.discord;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public class DiscordUtil {
    public static final String GUILD_ID = "1445820749473644568";
    public static final String WELCOME_CHANNEL_ID = "1445820750551580908";
    public static final String ANNOUNCEMENT_CHANNEL_ID = "1445820750551580912";
    public static final String MEMBERS_CHANNEL_ID = "1445820750551580905";
    public static final String PLAYERS_CHANNEL_ID = "1445820750551580906";

    public static final String ALERTS_CHANNEL_ID = "1445820750782009400";
    public static final String GAMES_CHANNEL_ID = "1485099512996302991";

    public static final String VOICE_CATEGORY_ID = "1445820751650361348";
    public static final String CREATE_YOUR_VOICE_CHANNEL_ID = "1445820751650361350";
    public static final String CONFIG_YOUR_VOICE_NAME = "\uD83E\uDD16┃config-ta-vocal";

    public static String ERROR = "<:red_right_arrow:1443279041032355880> ";
    public static String WARN = "<:yellow_right_arrow:1443279047525400618> ";
    public static String SUCCESS = "<:lime_right_arrow:1443279030454321364> ";
    public static String INFO = "<:gray_right_arrow:1443279024951394335> ";
    public static String NOTICE = "<:blue_right_arrow:1443279020040130691> ";

    public static String LINKED_ROLE_ID = "1480188185685266554";
    public static String ALERTS_ROLE_ID = "1485087495887650876";

    public static Optional<Guild> getGuild() {
        if (!DiscordAPI.isAvailable()) return Optional.empty();
        return Optional.ofNullable(DiscordAPI.getInstance().getJda().getGuildById(GUILD_ID));
    }
}
