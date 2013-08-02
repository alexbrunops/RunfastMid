package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ControllerListener implements OnTouchListener{

	private Gateway gateway;
	private Activity activity;
	private String character;
	private View[] views;
	private int[] inputCodes;
	
	public ControllerListener(Gateway gateway, Activity activity, String character) {
		this.gateway = gateway;
		this.activity = activity;
		this.character = character;
		//initRegularController();
		if(character.equals("pilot"))
			initPilotController();
		if(character.equals("copilot"))
			initCopilotController();
		
		inputCodes = new int[5];
	}
	
	/*
	private void initRegularController(){
		views = new View[9];
		views[0] = activity.findViewById(R.id.controller_up);
		views[1] = activity.findViewById(R.id.controller_down);
		views[2] = activity.findViewById(R.id.controller_left);
		views[3] = activity.findViewById(R.id.controller_right);
		views[4] = activity.findViewById(R.id.controller_action1);
		views[5] = activity.findViewById(R.id.controller_action2);
		views[6] = activity.findViewById(R.id.controller_plusLeft);
		views[7] = activity.findViewById(R.id.controller_plusRight);
		views[8] = activity.findViewById(R.id.controller_plusSelect);
	}
	*/
	
	private void initPilotController(){
		views = new View[8];
		views[0] = activity.findViewById(R.id.controller_up);
		views[1] = activity.findViewById(R.id.controller_down);
		views[2] = activity.findViewById(R.id.controller_left);
		views[3] = activity.findViewById(R.id.controller_right);
		views[4] = activity.findViewById(R.id.controller_action1);
		views[5] = activity.findViewById(R.id.controller_plusLeft);
		views[6] = activity.findViewById(R.id.controller_plusRight);
		views[7] = activity.findViewById(R.id.controller_plusSelect);
	}
	
	private void initCopilotController(){
		views = new View[6];
		views[0] = activity.findViewById(R.id.controller_up);
		views[1] = activity.findViewById(R.id.controller_down);
		views[2] = activity.findViewById(R.id.controller_left);
		views[3] = activity.findViewById(R.id.controller_right);
		views[4] = activity.findViewById(R.id.controller_action1);
		views[5] = activity.findViewById(R.id.controller_action2);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Estrange changes
		if(event.getAction()!=2)
			System.out.println("action = "+event.getAction());
		if((event.getAction() != MotionEvent.ACTION_DOWN)&&
				(event.getAction() != MotionEvent.ACTION_UP)&&
				((event.getAction()&MotionEvent.ACTION_MASK) != MotionEvent.ACTION_POINTER_DOWN)&&
				((event.getAction()&MotionEvent.ACTION_MASK) != MotionEvent.ACTION_POINTER_UP))
			return false;

		Map<String, Object> map = null;//new HashMap<String, Object>();

		if(event.getAction() == MotionEvent.ACTION_DOWN)//||
				//(event.getAction() == MotionEvent.ACTION_UP))
			map = getFirstPointerAction(v);
		else if((event.getAction()&MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN)
			map = getPointerAction(v, event);
		else
			map = getUpPointerAction(event);
		
		callServiceEvent(event, map);
		
		return true;
	}
	
	private void callServiceEvent(MotionEvent event, Map<String, Object> map){
		//Calls the middleware driver.
		try {
			if((event.getActionMasked() == MotionEvent.ACTION_DOWN)||
					((event.getAction()&MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN))
				gateway.callService(null, "inputPerformed",
						"br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver",
						null, null, map);
			else if((event.getActionMasked() == MotionEvent.ACTION_UP)||
					((event.getAction()&MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP))
				gateway.callService(null, "inputReleased",
						"br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver",
						null, null, map);
		} catch (ServiceCallException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, Object> getFirstPointerAction(View v){
		Map<String, Object> map = new HashMap<String, Object>();
		
		switch(v.getId()){
		case R.id.controller_up:
			System.out.println("UP!!! OKAY");
			map.put("inputCode", ""+0x01);//"UP");
			inputCodes[0] = 0x01;
			break;
		case R.id.controller_down:
			System.out.println("DOWN!!! OKAY");
			map.put("inputCode", ""+0x02);//"DOWN");
			inputCodes[0] = 0x02;
			break;
		case R.id.controller_left:
			System.out.println("LEFT!!! OKAY");
			map.put("inputCode", ""+0x03);//"LEFT");
			inputCodes[0] = 0x03;
			break;
		case R.id.controller_right:
			System.out.println("RIGHT!!! OKAY");
			map.put("inputCode", ""+0x04);//"RIGHT");
			inputCodes[0] = 0x04;
			break;
		case R.id.controller_action1:
			System.out.println("ACTION1!!! OKAY");
			map.put("inputCode", ""+0x05);//"ACTION1");
			inputCodes[0] = 0x05;
			break;
		case R.id.controller_action2:
			System.out.println("ACTION2!!! OKAY");
			map.put("inputCode", ""+0x06);//"ACTION2");
			inputCodes[0] = 0x06;
			break;
		case R.id.controller_plusLeft:
			System.out.println("plusLEFT!!! OKAY");
			map.put("inputCode", ""+0x11);//"plusLEFT");
			inputCodes[0] = 0x11;
			break;
		case R.id.controller_plusRight:
			System.out.println("plusRIGHT!!! OKAY");
			map.put("inputCode", ""+0x12);//"plusRIGHT");
			inputCodes[0] = 0x12;
			break;
		case R.id.controller_plusSelect:
			System.out.println("plusSELECT!!! OKAY");
			map.put("inputCode", ""+0x10);//"plusSELECT");
			inputCodes[0] = 0x10;
			break;
		default:
			System.out.println("DEFAULT!!!!!!!!!!!");
		}
		
		return map;
	}
	
	private Map<String, Object> getPointerAction(View v, MotionEvent event){
		System.out.println("POINTER ACTION!!!!!!!!!!!!!!");
		Map<String, Object> map = new HashMap<String, Object>();
		int action = event.getAction();
		int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		float x = event.getX(pointerIndex)+v.getLeft();
		float y = event.getY(pointerIndex)+v.getTop();
		
		System.out.println(" x "+x+" y "+y);
		//System.out.println("1- vx "+views[1].getLeft()+" vy "+views[1].getTop()+" vw "+views[1].getWidth()+" vh "+views[1].getHeight());
		
		boolean found = false;
		for(int i=0; (i<views.length)&&(!found); ++i){
			if((x>views[i].getLeft()) && (x<(views[i].getLeft()+views[i].getWidth()))&&
					(y>views[i].getTop()) && (y<(views[i].getTop()+views[i].getHeight()))){
				System.out.println(" entrou if - i = "+i);
				int inputCode = getCharacterInputCode(i);
				/*
				if(i<6)
					inputCode = i+1;
				else if(i==6)
					inputCode = 0x11;
				else if(i==7)
					inputCode = 0x12;
				else if(i==8)
					inputCode = 0x10;
				*/
				
				map.put("inputCode", ""+inputCode);
				
				inputCodes[pointerIndex] = inputCode;
				
				found = true;
			}
		}
		
		
		System.out.println("map.size = "+map.size());
		
		return map;
	}
	
	private int getCharacterInputCode(int n){
		int inputCode = 0;
		if(character.equals("pilot")){
			if(n<5)
				inputCode = n+1;
			else if(n==5)
				inputCode = 0x11;
			else if(n==6)
				inputCode = 0x12;
			else if(n==7)
				inputCode = 0x10;
		}else{
			if(n<6)
				inputCode = n+1;
		}
		
		return inputCode;
	}

	private Map<String, Object> getUpPointerAction(MotionEvent event){
		Map<String, Object> map = new HashMap<String, Object>();
		int action = event.getAction();
		int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		
		map.put("inputCode", ""+inputCodes[pointerIndex]);
		
		return map;
	}
}
