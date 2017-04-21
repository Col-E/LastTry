package org.egordorichev.lasttry.world.spawn.components;

import com.badlogic.gdx.Gdx;
import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.item.block.Block;
import org.egordorichev.lasttry.util.Camera;

/**
 * Created by Admin on 21/04/2017.
 */
public class GridCalculations {

    public static Area generateActiveArea() {
        Area activeAreaOfPlayer = new Area();

        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();
        int tww = windowWidth / Block.SIZE;
        int twh = windowHeight / Block.SIZE;

        // We want to get the further most position of x on the screen, camera is always in the middle so we
        // divide total window width by 2 and divide by blcok size to get grid position
        int tcx = (int) (Camera.game.position.x - windowWidth/2) / Block.SIZE;

        // TODO Change on inversion of y axis
        // We are subtracting because of the inverted y axis otherwise it would be LastTry.camera.position.y+windowheight/2
        int tcy = (int) (LastTry.world.getHeight() - (Camera.game.position.y + windowHeight/2)/Block.SIZE);

        // Checking to make sure y value is not less than 0 - World generated will always start from 0,0 top left.
        activeAreaOfPlayer.setMinYPoint(Math.max(0, tcy - 2));
        activeAreaOfPlayer.setMaxYPoint(Math.min(LastTry.world.getHeight() - 1, tcy + twh + 3));

        // Checking to make y values is not less than 0
        activeAreaOfPlayer.setMinXPoint(Math.max(0, tcx - 2));
        activeAreaOfPlayer.setMaxXPoint(Math.min(LastTry.world.getWidth() - 1, tcx + tww + 2));

        // Active zone is 6 greater
        // TODO Must check that it is not out of bou
        activeAreaOfPlayer.setMaxXPointActiveZone(activeAreaOfPlayer.getMaxXPoint()+25);

        return activeAreaOfPlayer;
    }



}
