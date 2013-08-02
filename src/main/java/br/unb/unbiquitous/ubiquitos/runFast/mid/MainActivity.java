package br.unb.unbiquitous.ubiquitos.runFast.mid;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity{

    private static final String TAG = "mid";
    
    private static final int MAKE_INVITE = 5;
    
    private Gateway gateway = null;
    //private UOSApplicationContext uosApplicationContext = null;
    
    private ToggleButton togMid;
    
    private LinearLayout llInvites;
    private Button searchRunFast;
    private Button acceptInvite, cancelInvite;
    private TextView receivedInvite;
    
    private final Handler handler = new Handler(){
    	  @Override
    	  public void handleMessage(Message msg) {
    	    if(msg.what==MAKE_INVITE){
    	    	receivedInvite.setVisibility(TextView.VISIBLE);
    	    	acceptInvite.setVisibility(Button.VISIBLE);
				cancelInvite.setVisibility(Button.VISIBLE);
    	    }
    	    super.handleMessage(msg);
    	  }
    	};
    
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
        setContentView(R.layout.main);

		initUI();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(togMid.isChecked()){
    		//togMid.setChecked(false);
    		llInvites.removeAllViews();
    		receivedInvite.setVisibility(TextView.INVISIBLE);
    		acceptInvite.setVisibility(Button.INVISIBLE);
			cancelInvite.setVisibility(Button.INVISIBLE);
			
			togMid.setChecked(false);
			togMid.setChecked(true);
		}
    }
    

    private void initUI(){
    	//Turns on/off the middleware
    	final Activity activity = this;
    	
    	togMid = (ToggleButton) findViewById(R.id.togMid);
    	togMid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	        if (isChecked) {
    	        	MidManager.getInstance(activity).startMiddleware();
    	        	gateway = MidManager.getGateway();
    	        	//uosApplicationContext = MidManager.getUosApplicationContext();
    	        	//startMiddleware();
    	        } else {
    	        	MidManager.getInstance(activity).stopMiddleware();
    	        	//stopMiddleware();
    	        }
    	    }
    	});
    	
		//Where the invites are created
		llInvites = (LinearLayout) findViewById(R.id.llInvites);
		
		/*
		 * Searches for one RFDEVICES_DRIVER in the list of drivers and if it
		 * finds it puts it in the list of games.
		 */
		searchRunFast = (Button) findViewById(R.id.runFastSearch);
		searchRunFast.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(gateway!=null){
					int time = 30;
					boolean found = false;
					//Takes the first game it finds ONLY
					while((time>0)&&(!found)){
						if(gateway.listDrivers(MidManager.RFDEVICES_DRIVER)!=null){
							System.out.println("Entrou no gateway.listDrivers()! "+
									gateway.listDrivers(MidManager.RFDEVICES_DRIVER).size());
							if(gateway.listDrivers(MidManager.RFDEVICES_DRIVER).size()>0)
								MidManager.setGameDevice(gateway.listDrivers(
										MidManager.RFDEVICES_DRIVER).get(0).getDevice());
								found = true;
						}
						try {//Waits a little and decreases counter of tries
							--time;
		            		Thread.sleep(1000);
		            	} catch (InterruptedException e) {
		            		System.out.println("interrupted");
		            	}
					}
					if(found)
						createInvite();
				}
					
			}
		});
		
		receivedInvite = (TextView) findViewById(R.id.tvReceivedInvite);
		
		acceptInvite = (Button) findViewById(R.id.btEnterInvite);
		acceptInvite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
				startActivity(intent);
			}
		});
		cancelInvite = (Button) findViewById(R.id.btCancelInvite);
		cancelInvite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				receivedInvite.setVisibility(TextView.INVISIBLE);
				acceptInvite.setVisibility(Button.INVISIBLE);
				cancelInvite.setVisibility(Button.INVISIBLE);
			}
		});
	}
    
    /**
     * Receive one invite from one device and creates an invite for it.
     * @param device
     */
    public void receiveInvite(UpDevice device){
    	if(gateway.listDrivers(MidManager.RFDEVICES_DRIVER)!=null){
    		if(gateway.listDrivers(MidManager.RFDEVICES_DRIVER).size()>0){
    			MidManager.setGameDevice(device);
    			//createInvite();
    			Message msg = handler.obtainMessage();
    		    msg.what = MAKE_INVITE;
    		    handler.sendMessage(msg);
    		}
    	}
    }
    
    
	/**
	 * Creates a question to the user choose if it wants to enter a game.
	 */
	private void createInvite(){
		LinearLayout line = new LinearLayout(getApplicationContext());
		line.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView tvName = new TextView(getApplicationContext());
		tvName.setText("Achado, quer jogar?");
		line.addView(tvName);
		
		Button btAccept,btDeny;
		btAccept = new Button(getApplicationContext());
		btAccept.setText("Sim");
		btDeny = new Button(getApplicationContext());
		btDeny.setText("Nao");
		btAccept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
				startActivity(intent);
			}
		});
		
		btDeny.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				llInvites.removeAllViews();
			}
		});
		
		line.addView(btAccept);
		line.addView(btDeny);
		
		llInvites.addView(line);
	}

}

