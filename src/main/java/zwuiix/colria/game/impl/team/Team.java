package zwuiix.colria.game.impl.team;

import cn.nukkit.item.Item;
import cn.nukkit.item.data.DyeColor;
import zwuiix.colria.translator.TranslationKeys;

public record Team(TranslationKeys name, Item reference, DyeColor dyeColor, String color) {
}