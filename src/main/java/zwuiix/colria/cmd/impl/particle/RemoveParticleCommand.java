package zwuiix.colria.cmd.impl.particle;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerParticleDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.particle.Particle;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;
import java.util.function.Consumer;

public class RemoveParticleCommand extends ColriaPlayerCommand {
    public RemoveParticleCommand() {
        super("removeparticle", "commands.removeparticle.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.PARTICLE_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new StringEnumArgument("particle", false, ParticleRegistry.getInstance().getParticles().keySet().toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        var identifier = args.get("particle").toString().toLowerCase();
        var particle = ParticleRegistry.getInstance().getParticle(identifier);
        if (particle == null) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_PARTICLE_NOEXIST, identifier);
            return;
        }

        DB.getPlayerDataInfo(target).then(info -> {
            processAsync(player, info.getXuid(), particle, target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }

    private void processAsync(EnginePlayer player, String xuid, Particle particle, String targetName) {
        if(xuid.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, targetName);
            return;
        }

        DataBase.getInstance()
                .query(PlayerParticleDao.class, dao -> dao.has(xuid, particle.getIdentifier()))
                .whenCompleteAsync((has, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    if(!has) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_PARTICLE_NOTHAS, targetName, player.processTranslation(particle.getName()));
                        return;
                    }

                    DataBase.getInstance().write(PlayerParticleDao.class, (Consumer<PlayerParticleDao>) dao -> dao.remove(xuid, particle.getIdentifier()));
                    EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(targetName);
                    if(targetPlayer != null) targetPlayer.resync();

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_PARTICLE_REMOVED, player.processTranslation(particle.getName()), targetName);
                });
    }
}
