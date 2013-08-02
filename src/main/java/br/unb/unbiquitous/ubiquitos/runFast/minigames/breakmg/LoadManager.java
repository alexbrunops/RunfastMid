package br.unb.unbiquitous.ubiquitos.runFast.minigames.breakmg;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import br.unb.unbiquitous.ubiquitos.runFast.mid.R;

public class LoadManager {

	private static Bitmap[] dynamite=null, gear=null, tire=null, sparkle=null, smoke=null, brokenCar=null;
	
	public LoadManager(Resources resources){
		if(dynamite==null)
			loadDynamite(resources);
		if(gear==null)
			loadGear(resources);
		if(tire==null)
			loadTire(resources);
		if(sparkle==null)
			loadSparkle(resources);
		if(smoke==null)
			loadSmoke(resources);
		if(brokenCar==null)
			loadBrokenCar(resources);
	}
	
	private void loadDynamite(Resources resources){
		dynamite = new Bitmap[1];
		dynamite[0] = BitmapFactory.decodeResource(resources, R.drawable.dynamite);
	}
	
	private void loadGear(Resources resources){
		gear = new Bitmap[3];
		gear[0] = BitmapFactory.decodeResource(resources, R.drawable.gear1);
		gear[1] = BitmapFactory.decodeResource(resources, R.drawable.gear2);
		gear[2] = BitmapFactory.decodeResource(resources, R.drawable.gear3);
	}

	private void loadTire(Resources resources){
		tire = new Bitmap[2];
		tire[0] = BitmapFactory.decodeResource(resources, R.drawable.tire1);
		tire[1] = BitmapFactory.decodeResource(resources, R.drawable.tire2);
	}
	
	private void loadSparkle(Resources resources){
		sparkle = new Bitmap[5];
		sparkle[0] = BitmapFactory.decodeResource(resources, R.drawable.sparkle1);
		sparkle[1] = BitmapFactory.decodeResource(resources, R.drawable.sparkle2);
		sparkle[2] = BitmapFactory.decodeResource(resources, R.drawable.sparkle3);
		sparkle[3] = BitmapFactory.decodeResource(resources, R.drawable.sparkle4);
		sparkle[4] = BitmapFactory.decodeResource(resources, R.drawable.sparkle5);
	}

	private void loadSmoke(Resources resources){
		smoke = new Bitmap[5];
		smoke[0] = BitmapFactory.decodeResource(resources, R.drawable.smoke1);
		smoke[1] = BitmapFactory.decodeResource(resources, R.drawable.smoke2);
		smoke[2] = BitmapFactory.decodeResource(resources, R.drawable.smoke3);
		smoke[3] = BitmapFactory.decodeResource(resources, R.drawable.smoke4);
		smoke[4] = BitmapFactory.decodeResource(resources, R.drawable.smoke5);
	}

	private void loadBrokenCar(Resources resources){
		brokenCar = new Bitmap[8];
		brokenCar[0] = BitmapFactory.decodeResource(resources, R.drawable.car1);
		brokenCar[1] = BitmapFactory.decodeResource(resources, R.drawable.car2);
		brokenCar[2] = BitmapFactory.decodeResource(resources, R.drawable.car3);
		brokenCar[3] = BitmapFactory.decodeResource(resources, R.drawable.car4);
		brokenCar[4] = BitmapFactory.decodeResource(resources, R.drawable.car5);
		brokenCar[5] = BitmapFactory.decodeResource(resources, R.drawable.car6);
		brokenCar[6] = BitmapFactory.decodeResource(resources, R.drawable.car7);
		brokenCar[7] = BitmapFactory.decodeResource(resources, R.drawable.car8);
	}
	
	/**
	 * @return all the images
	 */
	public Bitmap[] getImages(){
		Bitmap[] images = new Bitmap[6];
		images[0] = dynamite[0];
		images[1] = gear[0];
		images[2] = gear[1];
		images[3] = gear[2];
		images[4] = tire[0];
		images[5] = tire[1];
		
		return images;
	}
	
	/**
	 * @return the dynamite
	 */
	public Bitmap[] getDynamite() {
		return dynamite;
	}

	/**
	 * @return the gear
	 */
	public Bitmap[] getGear() {
		return gear;
	}

	/**
	 * @return the tire
	 */
	public Bitmap[] getTire() {
		return tire;
	}
	
	/**
	 * @return the sparkle
	 */
	public Bitmap[] getSparkle() {
		return sparkle;
	}

	/**
	 * @return the smoke
	 */
	public Bitmap[] getSmoke() {
		return smoke;
	}

	/**
	 * @return the brokenCar
	 */
	public Bitmap[] getBrokenCar() {
		return brokenCar;
	}
	
	
}
