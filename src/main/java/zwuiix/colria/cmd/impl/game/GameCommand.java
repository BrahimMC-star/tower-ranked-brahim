package zwuiix.colria.cmd.impl.game;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;

import java.util.Map;

public class GameCommand extends ColriaCommand {
    public GameCommand() {
        super("game", "Manage game");
    }

    @Override
    public void prepare() {
        registerSubCommand(new GameCreateSubCommand());
        registerSubCommand(new GameStartSubCommand());
        registerSubCommand(new GameDisbandSubCommand());

        registerSubCommand(new GameCoHostSubCommand());
        registerSubCommand(new GameEditSubCommand());
        registerSubCommand(new GamePrivacySubCommand());
        registerSubCommand(new GameWhitelistSubCommand());
        registerSubCommand(new GameBlacklistSubCommand());

        registerSubCommand(new GameFreezeSubCommand());
        registerSubCommand(new GameUnFreezeSubCommand());
        registerSubCommand(new GameSaySubCommand());
        registerSubCommand(new GameAnnounceSubCommand());

        registerSubCommand(new GameJoinSubCommand());
        registerSubCommand(new GameLeaveSubCommand());
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
