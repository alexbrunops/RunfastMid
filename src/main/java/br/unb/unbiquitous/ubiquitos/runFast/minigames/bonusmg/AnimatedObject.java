package br.unb.unbiquitous.ubiquitos.runFast.minigames.bonusmg;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class AnimatedObject {

	public static final int TYPE_COIN = 1; 
	public static final int TYPE_BOMB = 2; 
	
	private static final int COIN_FRAME_TIME = 33;
	private static final int BOMB_FRAME_TIME = 10;
	private static final int DEADCOIN_FRAME_TIME = 100;
	private static final int DEADBOMB_FRAME_TIME = 100;
	
	private static final int COIN_WH = 50;
	private static final int BOMB_WH = 50;
	
	private static final int VELOCITY_BOOST = 12;
	private static final int VELOCITY_MIN = 4;
	
	private float x,y;
	private int width,height;
	private Bitmap[] image,deadImage;
	private int time,frameTime,deadFrameTime,frame;
	private int type;
	private boolean dead;
	
	private float velocity;
	
	public AnimatedObject(float x, float y, Random random, LoadManager loadManager, int type){
		this.x = x;
		this.y = y;
		
		this.velocity = Math.abs(random.nextInt()%VELOCITY_BOOST)+VELOCITY_MIN;
		this.type = type;
		dead = false;
		load(loadManager);
	}
	
	public void load(LoadManager loadManager){
		time=frame=0;
		
		switch(type){
		case TYPE_COIN:
			image = loadManager.getCoin();
			deadImage = loadManager.getSparkle();
			frameTime = COIN_FRAME_TIME;
			deadFrameTime = DEADCOIN_FRAME_TIME;
			this.width = this.height = COIN_WH;
			break;
		case TYPE_BOMB:
			image = loadManager.getBomb();
			deadImage = loadManager.getSmoke();
			frameTime = BOMB_FRAME_TIME;
			deadFrameTime = DEADBOMB_FRAME_TIME;
			this.width = this.height = BOMB_WH;
			break;
		}
	}
	
	public void update(int dt){
	    time += dt;

	    int frameSkip;
	    
	    if(!dead){
	    	frameSkip = time/frameTime;
		    time = time%frameTime;
		    
		    frame += frameSkip;
		    
	    	if(frame >= image.length){
	    		frame = (frame - image.length)%image.length;
	    	}
	    	updateMovement();
	    }else{
	    	frameSkip = time/deadFrameTime;
		    time = time%deadFrameTime;
		    
		    frame += frameSkip;
	    	if(frame >= deadImage.length){
	    		frame = deadImage.length-1;
	    	}
	    }
	    
	}
	
	private void updateMovement(){
		y += velocity;
	}
	
	public void render(Canvas canvas){
		if(!dead)
			canvas.drawBitmap(image[frame], x, y, null);
		else
			canvas.drawBitmap(deadImage[frame], x, y, null);
	}
	
	public boolean isTouching(float x, float y){
		if((x >= this.x)&&(x <= this.x+width)
				&&(y >= this.y)&&(y <= this.y+height))
			return true;
		return false;
	}

	public void killAnimatedObject(){
		dead = true;
		time = frame = 0;
	}
	
	public boolean isDone(){
		if((dead)&&(frame == deadImage.length-1))
			return true;
		return false;
	}
	
	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	
}
