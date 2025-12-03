package zwuiix.colria.entity;

import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.Pet;

public interface EntityPet {
    void setInfo(Pet info);
    Pet getInfo();
    void setOwner(EnginePlayer owner);
    EnginePlayer getOwner();
    void setSaddled(boolean saddled);
    boolean isSaddled();
}