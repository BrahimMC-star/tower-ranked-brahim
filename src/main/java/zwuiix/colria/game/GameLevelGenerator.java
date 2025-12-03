package zwuiix.colria.game;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import zwuiix.colria.util.Fs;

import java.io.IOException;
import java.nio.file.Path;

public record GameLevelGenerator(String defaultLevel) {

    public Level create(String id) {
        var server = Server.getInstance();

        Level originalLevel = server.getLevelByName(defaultLevel);
        if (originalLevel == null) {
            server.loadLevel(defaultLevel);
            originalLevel = server.getLevelByName(defaultLevel);
        }

        if (originalLevel == null) {
            throw new IllegalArgumentException("Invalid level name: " + defaultLevel);
        }

        originalLevel.setTickRate(0);
        originalLevel.setRaining(false);
        originalLevel.setThundering(false);
        originalLevel.setAutoSave(false);
        originalLevel.save(true);

        Path originalPath = Path.of(server.getDataPath(), "worlds", originalLevel.getName());
        Path gameLevelPath = Path.of(server.getDataPath(), "worlds", originalLevel.getName() + "_" + id);

        try {
            Fs.copyDirectory(originalPath, gameLevelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!server.loadLevel(defaultLevel + "_" + id)) {
            throw new IllegalArgumentException("Invalid level name: " + defaultLevel);
        }

        Level gameLevel = server.getLevelByName(defaultLevel + "_" + id);
        if (gameLevel == null) {
            throw new IllegalArgumentException("Invalid level name: " + defaultLevel);
        }

        return gameLevel;
    }
}
