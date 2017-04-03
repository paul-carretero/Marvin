package motorsManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class Graber {
	private EV3LargeRegulatedMotor graber;
	private Port port;
	
	public Graber(){
		this.port   = LocalEV3.get().getPort(Main.GRABER);
		this.graber = new EV3LargeRegulatedMotor(this.port);
	}
	
	public void close(){
		this.graber.setSpeed(Main.GRABER_SPEED);
		this.graber.backward();
		
		syncWait(Main.GRABER_TIMER);
		
		this.graber.stop();
	}
	
	public void open(){
		this.graber.setSpeed(Main.GRABER_SPEED);
		this.graber.forward();
		syncWait(Main.GRABER_TIMER);
		this.graber.stop();
	}
	
	public void syncWait(int ms){
		synchronized (this) {
			try {
				this.wait(ms);
			} catch (InterruptedException e) {
				Main.printf("[GRABER]                : Finalisation de l'action du graber");
				try {
					this.wait(ms);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
				Thread.currentThread().interrupt();
			}
		}
	}

	public void stop() {
		this.graber.stop();
	}

}
