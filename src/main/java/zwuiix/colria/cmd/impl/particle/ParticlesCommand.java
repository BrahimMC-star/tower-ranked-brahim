package zwuiix.colria.cmd.impl.particle;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerParticleDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class ParticlesCommand extends ColriaPlayerCommand {
    public ParticlesCommand() {
        super("particles", "commands.particles.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.PARTICLE_MANAGE.toString());
        registerSubCommand(new ShowParticlesSubCommand());
        registerSubCommand(new AddParticleSubCommand());
        registerSubCommand(new RemoveParticleSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        throw new UnsupportedOperationException("This command requires a subcommand.");
    }
}
