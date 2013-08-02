package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetTCPConnectionManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import br.unb.unbiquitous.ubiquitos.runFast.minigames.bonusmg.BonusActivity;
import br.unb.unbiquitous.ubiquitos.runFast.minigames.breakmg.BreakActivity;
import dalvik.system.DexClassLoader;

public class MidManager {

	public static final String RFDEVICES_DRIVER = "br.unb.unbiquitous.ubiquitos.runFast.devicesControl.RFDevicesDriver";
	public static final String RFDEVICES_EVENT = "RFDevicesEvent";
	
	private static Gateway gateway = null;
	private static UOS uosApplicationContext = null;

	private static Activity activity = null;
	private static UpDevice gameDevice = null;
	
	private static String character = "assistant";
	
	private static MidManager instance = null;
	
	public static MidManager getInstance(Activity activity){
		MidManager.activity = activity;
		if(instance==null){
			instance = new MidManager();
		}
		return instance;
	}
	
	private MidManager(){}
	
	/**
	 * Starts the middleware.
	 */
	public void startMiddleware() {
		if(activity == null){
			return;
		}
		if(uosApplicationContext!=null){
			stopMiddleware();
		}
		
		/*
		File writableDir = activity.getApplicationContext().getDir("temp", Context.MODE_WORLD_WRITEABLE);
		File tempDir = null;
		try {
			tempDir = File.createTempFile("temp.owl", ""+System.nanoTime(),writableDir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		final File tempDir2 = tempDir;
		*/
		
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{"ubiquitos.connectionManager", EthernetTCPConnectionManager.class.getName()},
					{"ubiquitos.eth.tcp.port", "14984"},
					{"ubiquitos.eth.tcp.passivePortRange", "14985-15000"},
					{"ubiquitos.eth.udp.port", "15001"},
					{"ubiquitos.eth.udp.passivePortRange", "15002-15017"},
					{"ubiquitos.driver.deploylist", 
						RFInputDriver.class.getName()},//+";"+ExecutionDriver.class.getName()},
					//{"ubiquitos.ontology.path",tempDir2.getPath()},
		        };
			}
		};
		
		//setupClassloaderToolbox();
		
		ClassLoaderUtils.builder = new ClassLoaderUtils.DefaultClassLoaderBuilder(){
		 	 public ClassLoader getParentClassLoader() {
		 		 return activity.getClassLoader();
		 	 };
		};

		uosApplicationContext = new UOS();
		try {
			uosApplicationContext.init(prop);
			gateway = uosApplicationContext.getGateway();
		} catch (ContextException e) {
			e.printStackTrace();
		}
	}
	
	/*
	private void setupClassloaderToolbox() {
		ClassToolbox.platform = new ClassToolbox.Platform() {
			@Override
			protected File createTempDir() throws Exception {
				File writableDir = activity.getApplicationContext().getDir("temp", Context.MODE_WORLD_WRITEABLE);
				File tempDir = File.createTempFile("uExeTmp", ""+System.nanoTime(),writableDir);
				tempDir.delete(); // Delete temp file
				tempDir.mkdir();  // and transform it to a directory
				return tempDir;
			}

			@Override
			protected ClassLoader createClassLoader(File input) throws Exception {
				File folder = input.getParentFile();
				ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
				return new DexClassLoader(input.getPath(),folder.getPath(),null, parent);
			}
		};
	}
	*/
	
	/**
	 * Stops the middleware.
	 */
	public void stopMiddleware(){
		uosApplicationContext.tearDown();
		uosApplicationContext = null;
		gateway = null;
	}

	/**
	 * receiveInvite of a game device.
	 * @param device
	 */
	public static void receiveInvite(UpDevice device){
		if(activity!=null){
			if(activity instanceof MainActivity){
				((MainActivity) activity).receiveInvite(device);
			}
		}
	}
	
	/**
	 * End the game, finishing the current game activity.
	 * @param device
	 */
	public static void endGame(UpDevice device){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(!(activity instanceof MainActivity)){
					activity.finish();
				}
			}
		}
	}

	/**
	 * End the game, finishing the current game activity.
	 * @param device
	 */
	public static void endRace(UpDevice device){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(activity instanceof BonusActivity){
					endMGBonus(gameDevice, ((BonusActivity)activity).getScore());
				}else if(activity instanceof BreakActivity){
					endMGBreak(gameDevice);
				}
			}
		}
	}
	
	/**
	 * If it is in the ControllerActivity and is one "assistant"
	 * begins the Bonus minigame.
	 * @param device
	 */
	public static void beginMGBonus(UpDevice device){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(activity instanceof ControllerActivity){
					character = ((ControllerActivity) activity).getCharacter();
					Activity previous = activity;
					Intent intent = new Intent(activity.getApplicationContext(), BonusActivity.class);
					activity.startActivity(intent);
					previous.finish();
				}
			}
		}
	}

	/**
	 * If it is in the ControllerActivity and is one "assistant"
	 * begins the Bonus minigame.
	 * @param device
	 */
	public static void beginMGBreak(UpDevice device, int helpNumber){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(activity instanceof ControllerActivity){
					character = ((ControllerActivity) activity).getCharacter();
					Activity previous = activity;
					Intent intent = new Intent(activity.getApplicationContext(), BreakActivity.class);
					intent.putExtra("helpNumber", helpNumber);
					activity.startActivity(intent);
					previous.finish();
				}
			}
		}
	}
	
	public static void endMGBonus(UpDevice device, int bonus){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(activity instanceof BonusActivity){
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("deviceName", gateway.getCurrentDevice().getName());
						map.put("bonus", bonus);
						gateway.callService(MidManager.getGameDevice(), "bonusResult",
								MidManager.RFDEVICES_DRIVER, null, null, map);
					} catch (ServiceCallException e) {
						e.printStackTrace();
					}
					
					Activity previous = activity;
					Intent intent = new Intent(activity.getApplicationContext(), ControllerActivity.class);
					intent.putExtra("character", character);
					activity.startActivity(intent);
					previous.finish();
				}
			}
		}
	}
	
	public static void endMGBreak(UpDevice device){
		if(activity!=null){
			if(device.getName().equals(gameDevice.getName())){
				if(activity instanceof BreakActivity){
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("deviceName", gateway.getCurrentDevice().getName());
						gateway.callService(MidManager.getGameDevice(), "breakResult",
								MidManager.RFDEVICES_DRIVER, null, null, map);
					} catch (ServiceCallException e) {
						e.printStackTrace();
					}
					
					Activity previous = activity;
					Intent intent = new Intent(activity.getApplicationContext(), ControllerActivity.class);
					intent.putExtra("character", character);
					activity.startActivity(intent);
					previous.finish();
				}
			}
		}
	}
	
	/**
	 * @return the gateway
	 */
	public static Gateway getGateway() {
		return gateway;
	}

	/**
	 * @return the uosApplicationContext
	 */
	public static UOS getUosApplicationContext() {
		return uosApplicationContext;
	}

	/**
	 * @return the activity
	 */
	public static Activity getActivity() {
		return activity;
	}

	/**
	 * @param activity the activity to set
	 */
	public static void setActivity(Activity activity) {
		MidManager.activity = activity;
	}

	/**
	 * @return the gameDevice
	 */
	public static UpDevice getGameDevice() {
		return gameDevice;
	}

	/**
	 * @param gameDevice the gameDevice to set
	 */
	public static void setGameDevice(UpDevice gameDevice) {
		MidManager.gameDevice = gameDevice;
	}

}
