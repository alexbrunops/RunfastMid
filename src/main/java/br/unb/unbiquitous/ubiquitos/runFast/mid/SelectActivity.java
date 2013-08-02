package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectActivity extends Activity{

	private static String TAG = "SelectActivity";

	private Gateway gateway = null;
	
	private LinearLayout llOptions;
	private Button btNewTeam;
	private Button[] btsCopilots,btsAssistants;
	
	private SAListener listener;
	
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
        setContentView(R.layout.select_activity);
        
        MidManager.setActivity(this);
        gateway = MidManager.getGateway();
        createUI();
    }
	
	private void createUI(){
		llOptions = (LinearLayout)findViewById(R.id.select_llOptions);
		btNewTeam = (Button)findViewById(R.id.select_btNewTeam);
		
		ServiceResponse response = null;
		try {
			response = gateway.callService(MidManager.getGameDevice(), "getTeamsInfos",
					MidManager.RFDEVICES_DRIVER,null,null,null);
			System.out.println("numberOfTeams: "+response.getResponseData("numberOfTeams"));
		} catch (ServiceCallException e) {
			e.printStackTrace();
		}
		
		if(response!=null){
			createTeamsOptions(response);
			createNewTeamOption(response);
		}
	}
	
	/**
	 * createTeamsOptions
	 * @param response
	 */
	private void createTeamsOptions(ServiceResponse response){
		int i, size = Integer.decode((String)response.getResponseData("numberOfTeams"));
		btsCopilots = new Button[size];
		btsAssistants = new Button[size];
		listener = new SAListener();
		
		for(i=0; i<size; ++i){
			TextView tv = new TextView(getApplicationContext());
			tv.setText("Time "+(i+1));
			
			if(response.getResponseData("team"+i+"CoPilot").equals("false")){
				btsCopilots[i] = new Button(getApplicationContext());
				btsCopilots[i].setText("Entrar como Copiloto!");
				btsCopilots[i].setOnClickListener(listener);
			}
			btsAssistants[i] = new Button(getApplicationContext());
			btsAssistants[i].setText("Entrar como Assistente!");
			btsAssistants[i].setOnClickListener(listener);
			
			llOptions.addView(tv);
			if(btsCopilots[i]!=null)
				llOptions.addView(btsCopilots[i]);
			llOptions.addView(btsAssistants[i]);
		}
	}
	
	/**
	 * createNewTeamOptions
	 * @param response
	 */
	private void createNewTeamOption(ServiceResponse response){
		final int size = Integer.decode((String)response.getResponseData("numberOfTeams"));
		if(size <= 4){
			btNewTeam.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//New Team
					ServiceResponse response = null;
					try {
						response = gateway.callService(MidManager.getGameDevice(), "isInGame",
								MidManager.RFDEVICES_DRIVER, null, null, null);
					} catch (ServiceCallException e) {
						e.printStackTrace();
					}
					if(response!=null){
						if(response.getResponseString("isInGame").equals("true")){
							Intent intent = new Intent(getApplicationContext(), SelectCarActivity.class);
							startActivity(intent);
							finish();
						}else
							requestToJoinGame(size, "pilot");
							
					}
				}
			});
		}else{
			btNewTeam.setVisibility(Button.INVISIBLE);
		}
	}
	
	/**
	 * Make a request to the game to enter with the given parameters.
	 * @param teamNumber
	 * @param character
	 */
	private void requestToJoinGame(int teamNumber, String character){
		ServiceResponse response = null;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("deviceName", gateway.getCurrentDevice().getName());
			map.put("teamNumber", teamNumber);
			map.put("character", character);
			response = gateway.callService(MidManager.getGameDevice(), "requestPlayerJoin",
					MidManager.RFDEVICES_DRIVER, null, null, map);
		} catch (ServiceCallException e) {
			e.printStackTrace();
		}
		
		if(response!=null){
			if(response.getResponseString("hadJoined").equals("true")){
				// Intent intent = new Intent(getApplicationContext(), ControllerActivity.class);
				Intent intent = new Intent(getApplicationContext(), SelectController.class);
				intent.putExtra("character", character);
				startActivity(intent);
				finish();
			}else{
				//recreate
				startActivity(getIntent());
				finish();
			}
		}
	}
	
	/**
	 * SAListener is the buttons listener used to trigger the player
	 * entrance in the game getting into a team that already exists.
	 * 
	 * @author rafaelsimao
	 */
	public class SAListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			for(int i=0; i<btsCopilots.length; ++i){
				if(v==btsCopilots[i]){
					requestToJoinGame(i, "copilot");
					System.out.println("Copiloto "+(i+1));
				}
				if(v==btsAssistants[i]){
					requestToJoinGame(i, "assistant");
					System.out.println("Assistente "+(i+1));
				}
			}
		}
		
	}
}
