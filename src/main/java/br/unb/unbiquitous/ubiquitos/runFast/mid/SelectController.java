package br.unb.unbiquitous.ubiquitos.runFast.mid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectController extends Activity implements OnClickListener{
	
	Button classic, move;
	Intent intent = null;
	private String character;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_controller);
		
		character = getIntent().getStringExtra("character");
		
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		classic = (Button) findViewById(R.id.btClassic);
		move = (Button) findViewById(R.id.btMove);
		
		classic.setOnClickListener(this);
		move.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.btClassic:
			intent = new Intent(getApplicationContext(), ControllerActivity.class);
			intent.putExtra("character", character);
			startActivity(intent);
			finish();
			break;
			
		case R.id.btMove:
			intent = new Intent(getApplicationContext(), ControllerActivityMove.class);
			intent.putExtra("character", character);
			startActivity(intent);
			finish();
			break;
		}
	}

}
