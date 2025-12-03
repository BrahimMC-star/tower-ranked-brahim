package zwuiix.colria.cmd.impl.cosmetic;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;

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
        throw new IllegalArgumentException("This command requires a subcommand.");
    }
}
