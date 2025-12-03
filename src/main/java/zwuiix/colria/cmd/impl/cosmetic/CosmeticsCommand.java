package zwuiix.colria.cmd.impl.cosmetic;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class CosmeticsCommand extends ColriaPlayerCommand {
    public CosmeticsCommand() {
        super("cosmetics", "commands.cosmetics.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.COSMETIC_MANAGE.toString());

        registerSubCommand(new ShowCosmeticsSubCommand());
        registerSubCommand(new AddCosmeticSubCommand());
        registerSubCommand(new RemoveCosmeticSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        throw new UnsupportedOperationException("This command requires a subcommand.");
    }
}
