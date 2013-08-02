package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ControllerActivity extends Activity{

	private static String TAG = "controller";
	
	private Button left,right,up,down, action1,action2, plusLeft,plusRight,plusSelect;
	private Button quit;
	
	private Gateway gateway = null;
	
	private String character;
	
	/**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
        
		MidManager.setActivity(this);
		gateway = MidManager.getGateway();
		
		character = getIntent().getStringExtra("character");
        if(character.equals("pilot")){
        	setContentView(R.layout.pilot_controller);
        	initPilotUI();
        	initPilotListeners();
        }else if(character.equals("copilot")){
        	setContentView(R.layout.copilot_controller);
        	initCopilotUI();
        	initCopilotListeners();
        }else
        	setContentView(R.layout.assistant_controller);
        quit = (Button) findViewById(R.id.controller_quit);
        initQuitListener();
        	
		//setContentView(R.layout.controller);
        
    }
	
	@Override
	public void onBackPressed() {
		createQuitDialog().show();
	}
	
	private void initUI() {
		left = (Button) findViewById(R.id.controller_left);
		right = (Button) findViewById(R.id.controller_right);
		up = (Button) findViewById(R.id.controller_up);
		down = (Button) findViewById(R.id.controller_down);

		action1 = (Button) findViewById(R.id.controller_action1);
	}
	
	private void initCopilotUI(){
		initUI();
		action2 = (Button) findViewById(R.id.controller_action2);
	}
	
	private void initPilotUI(){
		initUI();
		action2 = (Button) findViewById(R.id.controller_action2);
		plusLeft = (Button) findViewById(R.id.controller_plusLeft);
		plusRight = (Button) findViewById(R.id.controller_plusRight);
		plusSelect = (Button) findViewById(R.id.controller_plusSelect);
	}
	
	private void initQuitListener(){
		final ControllerActivity activity = this;
		quit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.createQuitDialog().show();
			}
		});
	}
	
	private AlertDialog createQuitDialog(){
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(R.string.quitDialog_message)
			.setTitle(R.string.quitDialog_title)
			.setPositiveButton(R.string.quitDialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK button: call the quit service
					if(gateway.listDevices().contains(MidManager.getGameDevice()))
						try {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("deviceName", gateway.getCurrentDevice().getName());
							gateway.callService(MidManager.getGameDevice(), "playerQuit",
									MidManager.RFDEVICES_DRIVER, null, null, map);
						} catch (ServiceCallException e) {
							e.printStackTrace();
						}
					
					//Finishes this activity!
					finish();
				}
			})
			.setNegativeButton(R.string.quitDialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog: do nothing
				}
			});
				
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		
		return dialog;
	}
	
	
	private ControllerListener initListeners() {
		ControllerListener listener = new ControllerListener(MidManager.getGateway(),this,
				getIntent().getStringExtra("character"));
		left.setOnTouchListener(listener);
		right.setOnTouchListener(listener);
		up.setOnTouchListener(listener);
		down.setOnTouchListener(listener);
		
		action1.setOnTouchListener(listener);
		
		return listener;
	}
	
	private void initCopilotListeners(){
		ControllerListener listener = initListeners();
		action2.setOnTouchListener(listener);
	}
	
	private void initPilotListeners(){
		ControllerListener listener = initListeners();
		action2.setOnTouchListener(listener);
		plusLeft.setOnTouchListener(listener);
		plusRight.setOnTouchListener(listener);
		plusSelect.setOnTouchListener(listener);
	}

	/**
	 * @return the character
	 */
	public String getCharacter() {
		return character;
	}
	
}
