package org.egordorichev.lasttry.item.block.plant;

import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.graphics.Textures;
import org.egordorichev.lasttry.item.ItemID;

public class FireBlossom extends Plant {
    public FireBlossom() {
        super(ItemID.fireBlossom, "Fire Blossom", Assets.getTextureRegion(Textures.fireBlossomIcon), Assets.getTextureRegion(Textures.fireBlossom));
    }

    @Override
    public boolean canBeGrownAt(int x, int y) {
        if (!super.canBeGrownAt(x, y)) {
            return false;
        }

        short id = Globals.world.blocks.getID(x, y + 1);

        if (id == ItemID.ashBlock) {
            return true;
        }

        return false;
    }

    @Override
    public void updateBlock(int x, int y) {
        int hp = Globals.world.blocks.getHP(x, y);

        if (hp >= Plant.GROW_THRESHOLD) {
            if (Globals.environment.time.getHour() >= 4 && Globals.environment.time.getHour() <= 7
                    && !Globals.environment.isRaining()) { // TODO: from 3:45 to 7:30

                Globals.world.blocks.setHP((byte) (Plant.GROW_THRESHOLD + 1), x, y);
            } else {
                Globals.world.blocks.setHP((byte) (Plant.GROW_THRESHOLD), x, y);
            }
        } else {
            Globals.world.blocks.setHP((byte) (hp + 1), x, y);
        }
    }
}