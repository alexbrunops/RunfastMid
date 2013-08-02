package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.NotifyException;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

public class RFInputDriver implements UosEventDriver{

	private static Logger logger = Logger.getLogger(RFInputDriver.class.getName());

	public static final String RFINPUT_DRIVER = "br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver";
    
	public static final String RFINPUT_EVENT = "RFInputEvent";
    public static final String RFINPUT_PERFORMED_PARAM = "isPerformed";
    public static final String RFINPUT_IC_CODE = "inputCode";
    public static final String RFINPUT_IC_INTENSITY = "intensity";
    
    public static final String RF_DEVNAME_PARAM = "deviceName";
    
	private Gateway gateway;
	private String instanceId;
	
	private int numberOfListeners = 0;

    private Vector<UpNetworkInterface> listenerDevices = new Vector<UpNetworkInterface>();
	
	@Override
	public void destroy() {}

	@Override
	public UpDriver getDriver() {
		UpDriver driver = new UpDriver(RFINPUT_DRIVER);
		
		driver.addService("receiveInvite")
			.addParameter("deviceName", ParameterType.MANDATORY);
		driver.addService("endGame")
			.addParameter("deviceName", ParameterType.MANDATORY);
		driver.addService("endRace")
			.addParameter("deviceName", ParameterType.MANDATORY);
		
		driver.addService("beginMGBonus")
			.addParameter("deviceName", ParameterType.MANDATORY);
		driver.addService("beginMGBreak")
			.addParameter("deviceName", ParameterType.MANDATORY)
			.addParameter("helpNumber", ParameterType.MANDATORY);
		
		driver.addService("startSpin")
			.addParameter("intensity", ParameterType.MANDATORY);
		driver.addService("stopSpin");
		
		driver.addService("inputPerformed")
			.addParameter("inputCode", ParameterType.MANDATORY);
		driver.addService("inputReleased")
			.addParameter("inputCode", ParameterType.MANDATORY);
		
		return driver;
	}

	@Override
	public List<UpDriver> getParent() {
		return new ArrayList<UpDriver>();
	}

	@Override
	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
		this.instanceId = instanceId;
	}

	/**
	 * Register new listeners.
	 */
	@Override
	public void registerListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		NetworkDevice networkDevice = messageContext.getCallerDevice();
	       
        UpNetworkInterface uni = new UpNetworkInterface(
                networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());
       
        if (!listenerDevices.contains(uni)){
            listenerDevices.addElement(uni);
        }
        
        logger.info("Device "+networkDevice.getNetworkDeviceName()+" registered as a listenner");

        numberOfListeners++;
        //kbDriverGUI.changeNumberOfListeners(numberOfListeners);
	}

	/**
	 * Unregister some listener.
	 */
	@Override
	public void unregisterListener(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		NetworkDevice networkDevice = messageContext.getCallerDevice();
	       
        UpNetworkInterface uni = new UpNetworkInterface(
                networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());
        listenerDevices.removeElement(uni);
        
        logger.info("Device "+networkDevice.getNetworkDeviceName()+" unregistered from the listenners list");

        numberOfListeners--;
        //kbDriverGUI.changeNumberOfListeners(numberOfListeners);
	}

	//Talk to Game Services
	/**
	 * Receive one invite from one game and sends it to the activity
	 * so that the user can enter it.
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void receiveInvite(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		MidManager.receiveInvite(getDevice(serviceCall.getParameterString("deviceName")));
	}

	/**
	 * Receive one invite from one game and sends it to the activity
	 * so that the user can enter it.
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void endGame(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		MidManager.endGame(getDevice(serviceCall.getParameterString("deviceName")));
	}
	
	/**
	 * Receive one invite from one game and sends it to the activity
	 * so that the user can enter it.
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void endRace(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		MidManager.endRace(getDevice(serviceCall.getParameterString("deviceName")));
	}
	
	/**
	 * Begins the Bonus mini-game
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void beginMGBonus(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		MidManager.beginMGBonus(getDevice(serviceCall.getParameterString("deviceName")));
	}

	/**
	 * Begins the Break mini-game
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void beginMGBreak(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		MidManager.beginMGBreak(getDevice(serviceCall.getParameterString("deviceName")),
				Integer.parseInt(serviceCall.getParameterString("helpNumber")));
	}

	
	/**
	 * Finds the correspondent UpDevice with the given name and returns it
	 * otherwise if it does not find it returns null;
	 * @param deviceName
	 * @return 
	 */
	public UpDevice getDevice(String deviceName){
		boolean found = false;
		int i = 0;
		UpDevice device = null;
		while((!found)&&(i<gateway.listDevices().size())){
			if(gateway.listDevices().get(i).getName().equals(deviceName)){
				device = gateway.listDevices().get(i);
				found = true;
			}
			++i;
		}
		return device;
	}
	
	//Intensity Input Services
	/**
	 * Notifies a startSpin
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void startSpin(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		System.out.println("PARAMETER-Intensity: "+serviceCall.getParameter("intensity"));
		notifyInput(""+0x21, true, Double.parseDouble(serviceCall.getParameterString("intensity")));
	}
	/**
	 * Notifies a stopSpin
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void stopSpin(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		System.out.println("PARAMETER-Intensity: stop");
		notifyInput(""+0x21, false, 0.0);
	}
	
	//Input Services
	/**
	 * Notifies a inputPerformed
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void inputPerformed(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		System.out.println("PARAMETER: "+serviceCall.getParameter("inputCode"));
		notifyInput(serviceCall.getParameterString("inputCode"), true, 0.0);
	}
	
	/**
	 * Notifies a inputReleased
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void inputReleased(ServiceCall serviceCall, 
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		System.out.println("PARAMETER: "+serviceCall.getParameter("inputCode"));
		notifyInput(serviceCall.getParameterString("inputCode"), false, 0.0);
	}
	

	/**
	 * Notify some input passing as parameter if the input had been performed,
	 * the input code and the current device which is performing the input. 
	 * @param messageToSend
	 * @param performed
	 */
	private void notifyInput(String messageToSend, boolean performed, double intensity) {
		Notify notify = new Notify(RFINPUT_EVENT);

		notify.addParameter(RFINPUT_PERFORMED_PARAM, ""+performed);
        notify.addParameter(RFINPUT_IC_CODE, messageToSend);
        notify.addParameter(RFINPUT_IC_INTENSITY, ""+intensity);
        notify.addParameter(RF_DEVNAME_PARAM, gateway.getCurrentDevice().getName());
        notify.setDriver(RFINPUT_DRIVER);
        notify.setInstanceId(instanceId);
        
        logger.info("Sending message: "+messageToSend+" - performed:"+performed+" - device: "+gateway.getCurrentDevice().getName());

        notifyEvent(notify);
        
	}
	
	/**
	 * Sends the notify to the environment.
	 * @param notify
	 */
	private void notifyEvent(Notify notify){
		
		for (int i = 0 ; i < listenerDevices.size(); i++){
            UpNetworkInterface uni = (UpNetworkInterface) listenerDevices.elementAt(i);
            UpDevice device = new UpDevice("Anonymous");
            device.addNetworkInterface(uni.getNetworkAddress(), uni.getNetType());
            
            try {
                this.gateway.sendEventNotify(notify, device);
            } catch (NotifyException e) {
            	//logger.error(e.getMessage());
            }
        }
	}

	
	/**
	 * @return the numberOfListeners
	 */
	public int getNumberOfListeners() {
		return numberOfListeners;
	}
	
}
