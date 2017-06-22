package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop extends ApplicationAdapter {
	private Texture asteroidaImg;
	private Texture statekImg;
	private Texture laserImg;
	//	private Sound dropSound;
//	private Music rainMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle statek;
	private Array<Rectangle> asteroidy;
    private Array<Rectangle> lasery;
	private long lastDropTime;
    private long poprzedniStrzal = 0;

	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		asteroidaImg = new Texture(Gdx.files.internal("asteroida.png"));
		statekImg = new Texture(Gdx.files.internal("spaceship.png"));
        laserImg = new Texture(Gdx.files.internal("laser.png"));
		// load the drop sound effect and the rain background "music"
		//dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		//rainMusic.setLooping(true);
		//rainMusic.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1240, 720);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the bucket
		statek = new Rectangle();
		statek.x = 1240 / 2 - 90 / 2; // center the bucket horizontally
		statek.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
		statek.width = 90;
		statek.height = 150;

		// create the raindrops array and spawn the first raindrop
		asteroidy = new Array<Rectangle>();
        lasery = new Array<Rectangle>();
		spawnAsteroida();
	}

	private void spawnAsteroida() {
		Rectangle asteroida = new Rectangle();
		asteroida.x = MathUtils.random(0, 1240-170);
		asteroida.y = 720;
		asteroida.width = 110;
		asteroida.height = 100;
		asteroidy.add(asteroida);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		batch.begin();
		batch.draw(statekImg, statek.x, statek.y);
		for(Rectangle asteroida: asteroidy) {
			batch.draw(asteroidaImg, asteroida.x, asteroida.y);
		}
		for(Rectangle laser: lasery) {
            batch.draw(laserImg,laser.x,laser.y);
        }
		batch.end();

		// process user input
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			statek.x = touchPos.x - 90 / 2;
            //strzal();
		}
		if(Gdx.input.isKeyPressed(Keys.LEFT)) statek.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) statek.x += 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.UP)) strzal();

		// make sure the bucket stays within the screen bounds
		if(statek.x < 0) statek.x = 0;
		if(statek.x > 1240 - 90) statek.x = 1240 - 90;

		// check if we need to create a new raindrop
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnAsteroida();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the later case we play back
		// a sound effect as well.
        Iterator<Rectangle> iterLasery = lasery.iterator();
        while(iterLasery.hasNext()) {
            Rectangle laser = iterLasery.next();
            laser.y += 200 * Gdx.graphics.getDeltaTime();
            if(laser.y > 720) iterLasery.remove();
        }

		Iterator<Rectangle> iter = asteroidy.iterator();
		while(iter.hasNext()) {
			Rectangle asteroida = iter.next();
			asteroida.y -= 150 * Gdx.graphics.getDeltaTime();
			if(asteroida.y + 100 < 0) iter.remove();
			if(asteroida.overlaps(statek)) {
				//	dropSound.play();
				iter.remove();
			}
		}
	}

    private void strzal() {
        Rectangle laser = new Rectangle();
        if(poprzedniStrzal == 0) {
            laser.x = (statek.x + statek.width / 2) - 26 / 2;
            laser.y = statek.height;
            laser.width = 26;
            laser.height = 90;
        }
        else if(TimeUtils.millis() - poprzedniStrzal > 1000) {
            laser.x = (statek.x + statek.width / 2) - 26 / 2;
            laser.y = statek.height;
            laser.width = 26;
            laser.height = 90;
        }
        lasery.add(laser);
        poprzedniStrzal = TimeUtils.millis();
    }

    @Override
	public void dispose() {
		// dispose of all the native resources
		statekImg.dispose();
		asteroidaImg.dispose();
        laserImg.dispose();
		//dropSound.dispose();
		//	rainMusic.dispose();
		batch.dispose();
	}
}