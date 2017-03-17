package motorsManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class Graber {
	private EV3LargeRegulatedMotor graber;
	private Port port;
	
	public Graber(){
		port   = LocalEV3.get().getPort(Main.GRABER);
		graber = new EV3LargeRegulatedMotor(port);
	}
	
	public void close(){
		if(Main.getState(Main.HAND_OPEN)){
			graber.setSpeed(Main.GRABER_SPEED);
			graber.backward();
			syncWait(Main.GRABER_TIMER);
			graber.stop();
		}
	}
	
	public void open(){
		if(!Main.getState(Main.HAND_OPEN)){
			graber.setSpeed(Main.GRABER_SPEED);
			graber.forward();
			syncWait(Main.GRABER_TIMER);
			graber.stop();
		}
	}
	
	public void syncWait(int ms){
		synchronized (this) {
			try {
				this.wait(ms);
			} catch (InterruptedException e) {
				Main.printf("Finalisation de l'action du graber");
				try {
					this.wait(ms);
				} catch (InterruptedException e1) {
					// do nothing \O/
				}
				Thread.currentThread().interrupt();
			}
		}
	}

	public void stop() {
		graber.stop();
	}

}
