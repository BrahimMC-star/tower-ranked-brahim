package zwuiix.colria.util;

import cn.nukkit.Server;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerDataDao;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.PlayerDataInfo;

public class DB {
    public static Promise<PlayerDataInfo> getPlayerDataInfoFromDiscordId(String discordId) {
        var promise = new Promise<PlayerDataInfo>();

        DataBase.getInstance()
                .query(PlayerDataDao.class, dao -> dao.getFromDiscordId(discordId))
                .exceptionally(err -> {
                    promise.reject(err);
                    return null;
                })
                .whenCompleteAsync((info, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    promise.resolve(info);
                });
        return promise;
    }

    public static Promise<PlayerDataInfo> getPlayerDataInfo(String name) {
        var promise = new Promise<PlayerDataInfo>();

        EnginePlayer onlineTarget = (EnginePlayer) Server.getInstance().getPlayerExact(name);
        if(onlineTarget != null) {
            promise.resolve(onlineTarget.getPlayerDataInfo());
            return promise;
        }

        DataBase.getInstance()
                .query(PlayerDataDao.class, dao -> dao.get(name))
                .exceptionally(err -> {
                    promise.reject(err);
                    return null;
                })
                .whenCompleteAsync((info, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    promise.resolve(info);
                });
        return promise;
    }

    public static Promise<PlayerDataInfo> getPlayerDataInfoXuid(String xuid) {
        var promise = new Promise<PlayerDataInfo>();

        DataBase.getInstance()
                .query(PlayerDataDao.class, dao -> dao._get(xuid))
                .exceptionally(err -> {
                    promise.reject(err);
                    return null;
                })
                .whenCompleteAsync((info, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    promise.resolve(info);
                });
        return promise;
    }
}
