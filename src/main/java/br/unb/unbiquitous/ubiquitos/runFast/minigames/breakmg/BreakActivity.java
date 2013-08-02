package br.unb.unbiquitous.ubiquitos.runFast.minigames.breakmg;

import br.unb.unbiquitous.ubiquitos.runFast.mid.MidManager;
import android.app.Activity;
import android.os.Bundle;

public class BreakActivity extends Activity {

	private GameSurface gameSurface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MidManager.setActivity(this);
		
		int helpNumber = getIntent().getIntExtra("helpNumber",1);
		gameSurface = new GameSurface(getApplicationContext(),getResources(),helpNumber);
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

}
