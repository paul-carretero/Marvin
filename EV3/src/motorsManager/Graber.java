package motorsManager;

import aiPlanner.Main;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class Graber {
	private EV3LargeRegulatedMotor	graber;
	private Port					port;
	private WaitProvider			manager;
	
	public Graber(WaitProvider manager){
		this.port   	= LocalEV3.get().getPort(Main.GRABER);
		this.graber 	= new EV3LargeRegulatedMotor(this.port);
		this.manager	= manager;
	}
	
	public void close(){
		this.graber.setSpeed(Main.GRABER_SPEED);
		this.graber.backward();
		
		this.manager.syncWait(Main.GRABER_TIMER);
		
		this.graber.stop();
	}
	
	public void open(){
		this.graber.setSpeed(Main.GRABER_SPEED);
		this.graber.forward();
		this.manager.syncWait(Main.GRABER_TIMER);
		this.graber.stop();
	}
	
	public void stop() {
		this.graber.stop();
	}

}
