package br.unb.unbiquitous.ubiquitos.runFast.minigames.breakmg;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class AnimatedObject {

	public static final int TYPE_DYNAMITE = 1; 
	public static final int TYPE_GEAR = 2; 
	public static final int TYPE_TIRE = 3; 
	
	public static final int SCORE_DYNAMITE = -20; 
	public static final int SCORE_GEAR = 6; 
	public static final int SCORE_TIRE = 10;
	
	private static final int DEFAULT_FRAME_TIME = 10;
	
	private static final int DEADSMOKE_FRAME_TIME = 100;
	private static final int DEADSPARKLE_FRAME_TIME = 100;
	
	private float x,y;
	private int width,height;
	private Bitmap[] image,deadImage;
	private int time,frameTime,deadFrameTime,frame, lifeTime;
	private int type, score;
	private boolean dead;
	
	public AnimatedObject(float x, float y, Random random, LoadManager loadManager){
		this.x = x;
		this.y = y;
		
		initType(random);
		lifeTime = (Math.abs(random.nextInt()%5)+2)*1000;
		dead = false;
		load(loadManager,random);
	}
	
	private void initType(Random random){
		int newType = Math.abs(random.nextInt()%10);
		if(newType < 3){
			type = TYPE_DYNAMITE;
			score = SCORE_DYNAMITE;
		}else if(newType <7){
			type = TYPE_GEAR;
			score = SCORE_GEAR;
		}else{
			type = TYPE_TIRE;
			score = SCORE_TIRE;
		}
	}
	
	public void load(LoadManager loadManager, Random random){
		time=frame=0;

		Bitmap[] imageHelp;
		switch(type){
		case TYPE_DYNAMITE:
			image = loadManager.getDynamite();
			frameTime = DEFAULT_FRAME_TIME;
			deadImage = loadManager.getSmoke();
			deadFrameTime = DEADSMOKE_FRAME_TIME;
			break;
		case TYPE_GEAR:
			imageHelp = loadManager.getGear();
			image = new Bitmap[1];
			image[0] = imageHelp[Math.abs(random.nextInt()%imageHelp.length)];
			frameTime = DEFAULT_FRAME_TIME;
			
			deadImage = loadManager.getSparkle();
			deadFrameTime = DEADSPARKLE_FRAME_TIME;
			break;
		case TYPE_TIRE:
			imageHelp = loadManager.getTire();
			image = new Bitmap[1];
			image[0] = imageHelp[Math.abs(random.nextInt()%imageHelp.length)];
			frameTime = DEFAULT_FRAME_TIME;
			
			deadImage = loadManager.getSparkle();
			deadFrameTime = DEADSPARKLE_FRAME_TIME;
			break;
		}
		this.width = image[0].getWidth();
		this.height = image[0].getHeight();
	}
	
	public void update(int dt){
		if(!dead)
	    	lifeTime -= dt;

		time += dt;

	    int frameSkip;
	    
	    if(!dead){
	    	frameSkip = time/frameTime;
		    time = time%frameTime;
		    
		    frame += frameSkip;
		    
	    	if(frame >= image.length){
	    		frame = (frame - image.length)%image.length;
	    	}
	    }else{
	    	frameSkip = time/deadFrameTime;
		    time = time%deadFrameTime;
		    
		    frame += frameSkip;
	    	if(frame >= deadImage.length){
	    		frame = deadImage.length-1;
	    	}
	    }
	    
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
		if((!dead) && (lifeTime < 0))
			return true;
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

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

}
