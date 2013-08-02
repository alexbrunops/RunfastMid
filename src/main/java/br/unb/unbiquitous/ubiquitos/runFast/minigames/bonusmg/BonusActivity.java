package br.unb.unbiquitous.ubiquitos.runFast.minigames.bonusmg;

import br.unb.unbiquitous.ubiquitos.runFast.mid.MidManager;
import android.app.Activity;
import android.os.Bundle;

public class BonusActivity extends Activity {

	private GameSurface gameSurface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MidManager.setActivity(this);
		
		gameSurface = new GameSurface(getApplicationContext(),getResources());
		setContentView(gameSurface);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		gameSurface.pause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		gameSurface.resume();
	}

	public int getScore(){
		if(gameSurface!=null)
			return gameSurface.getScore();
		return 0;
	}
}
