package zwuiix.colria.discord;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public class ServerInfo {
    public static final String GUILD_ID = "1437088781424394302";
    public static final String WELCOME_CHANNEL_ID = "1437088782791868457";
    public static final String ANNOUNCEMENT_CHANNEL_ID = "1437088782791868460";
    public static final String MEMBERS_CHANNEL_ID = "1438129566722818112";
    public static final String PLAYERS_CHANNEL_ID = "1438129706212921414";

    public static final String VOICE_CATEGORY_ID = "1437088782791868463";
    public static final String CREATE_YOUR_VOICE_CHANNEL_ID = "1443292066460340244";
    public static final String CONFIG_YOUR_VOICE_NAME = "\uD83E\uDD16┃config-ta-vocal";

    public static String ERROR = "<:red_right_arrow:1443279041032355880> ";
    public static String WARN = "<:yellow_right_arrow:1443279047525400618> ";
    public static String SUCCESS = "<:lime_right_arrow:1443279030454321364> ";
    public static String INFO = "<:gray_right_arrow:1443279024951394335> ";
    public static String NOTICE = "<:blue_right_arrow:1443279020040130691> ";

    public static Optional<Guild> getGuild() {
        return Optional.ofNullable(DiscordAPI.getInstance().getJda().getGuildById(GUILD_ID));
    }
}
