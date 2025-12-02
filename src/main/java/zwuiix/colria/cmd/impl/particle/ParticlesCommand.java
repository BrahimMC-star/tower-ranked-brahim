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
        setPermission(Permission.RANK_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();

        DB.getPlayerDataInfo(target).then(info -> {
            processAsync(player, info.getXuid(), target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }

    private void processAsync(EnginePlayer player, String xuid, String targetName) {
        if (xuid.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, targetName);
            return;
        }

        DataBase.getInstance()
                .query(PlayerParticleDao.class, dao -> dao.listByXuid(xuid))
                .whenCompleteAsync((particles, ex) -> {
                    if (ex != null) {
                        ex.printStackTrace();
                    }

                    if (particles.isEmpty()) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_PARTICLE_LIST_EMPTY, targetName);
                        return;
                    }

                    StringBuilder names = new StringBuilder();
                    for (String identifier : particles) {
                        var particle = ParticleRegistry.getInstance().getParticle(identifier);
                        if (particle != null) {
                            if (!names.isEmpty()) names.append("§r§7, ");
                            names.append(player.processTranslation(particle.getName()));
                        }
                    }

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_PARTICLE_LIST, targetName, names.toString());
                });
    }
}
