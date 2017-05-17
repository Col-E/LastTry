package org.egordorichev.lasttry.item.block.plant;

import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.graphics.Textures;
import org.egordorichev.lasttry.item.ItemID;

public class MoonGlow extends Plant {
    public MoonGlow() {
        super(ItemID.moonGlow, "Moon Glow", Assets.getTextureRegion(Textures.moonGlowIcon), Assets.getTextureRegion(Textures.moonGlow));
    }

    @Override
    public void updateBlock(int x, int y) {
        int hp = Globals.world.blocks.getID(x, y);

        if (hp >= Plant.GROW_THRESHOLD) {
            if (Globals.environment.time.isNight()) {
                Globals.world.blocks.setHP((byte) (Plant.GROW_THRESHOLD + 1), x, y);
            } else {
                Globals.world.blocks.setHP((byte) (Plant.GROW_THRESHOLD), x, y);
            }
        } else {
            Globals.world.blocks.setHP((byte) (hp + 1), x, y);
        }
    }

    @Override
    public boolean canBeGrownAt(int x, int y) {
        if (!super.canBeGrownAt(x, y)) {
            return false;
        }

        short id = Globals.world.blocks.getHP(x, y - 1);

        if (id != ItemID.jungleGrassBlock) {
            return false;
        }

        return true;
    }
}