package zwuiix.colria.player;

import cn.nukkit.network.protocol.types.SortOrder;
import cn.nukkit.scoreboard.scoreboard.Scoreboard;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.util.Glyph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerScoreboard extends Scoreboard {
    private final EnginePlayer player;
    @Setter
    @Getter
    private ArrayList<String> texts = new ArrayList<>();

    public PlayerScoreboard(EnginePlayer player) {
        super(player.getName(), Glyph.translate("colria", EngineInfo.COLOR), "dummy", SortOrder.ASCENDING);
        this.player = player;
    }

    public void title(String title) {
        this.displayName = Glyph.translate(title, EngineInfo.COLOR);
    }

    public void update(int index, String line) {
        this.texts.add(index, line);
    }

    public void updates(List<String> lines) {
        this.texts.clear();
        this.texts.addAll(lines);
    }

    public void updates(String ...lines) {
        this.texts.clear();
        this.texts.addAll(Arrays.asList(lines));
    }

    public void sync() {
        if(!player.spawned) return;

        ArrayList<String> lines = new ArrayList<>();
        lines.add(TextFormat.WHITE + Glyph.hbarThick(TextFormat.DARK_GRAY, 1));
        lines.addAll(texts);
        lines.add(TextFormat.MATERIAL_RESIN.toString());
        lines.add(EngineInfo.COLOR + EngineInfo.DOMAIN);
        lines.add(TextFormat.RESET + Glyph.hbarThick(TextFormat.DARK_GRAY, 1));

        player.scoreboard.setLines(lines);
    }

    public void clear() {
        if(!player.spawned) return;
        texts.clear();
        sync();
    }
}
