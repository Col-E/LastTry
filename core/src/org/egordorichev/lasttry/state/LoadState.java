package org.egordorichev.lasttry.state;

import com.badlogic.gdx.Gdx;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.core.Bootstrap;
import org.egordorichev.lasttry.entity.player.PlayerIO;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.world.World;
import org.egordorichev.lasttry.world.WorldIO;
import org.egordorichev.lasttry.world.environment.Environment;
import org.egordorichev.lasttry.world.spawn.SpawnSystem;

public class LoadState implements State {
    private boolean loaded = false;
    private String loadString = "";

    public LoadState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
	                    Bootstrap.load();
                        loadString = "Loading spawn system...";
                        Globals.spawnSystem = new SpawnSystem();
                        loadString = "Loading environment...";
                        Globals.environment = new Environment();
                        loadString = "Loading world...";

                        if (WorldIO.saveExists("test")) {
                        	WorldIO.load("test");
                        } else {
                        	Globals.world = WorldIO.generate("test", World.Size.SMALL, 0);
                        }

                        loadString = "Loading player...";

                        if (PlayerIO.saveExists("test")) {
	                        PlayerIO.load("test");
                        } else {
	                        Globals.player = PlayerIO.generate("test");
                        }

                        loaded = true;
                    }
                });
            }
        }).start();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (this.loaded) {
            LastTry.instance.setScreen(new GamePlayState());
            return;
        }

        for (int i = 0; i < Gdx.graphics.getWidth() / 48 + 1; i++) {
            Graphics.batch.draw(Graphics.skyTextureRegion, i * 48, 0);
        }

        Assets.f22.draw(Graphics.batch, this.loadString, 0, 0);
        LastTry.ui.render();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}