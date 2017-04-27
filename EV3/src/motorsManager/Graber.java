package motorsManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

/**
 * Fournit des primitives pour les actions du graber (grab, fermer et stop).
 * @author paul.carretero
 */
public class Graber {
	/**
	 * Représentation LeJos du Graber
	 */
	private EV3LargeRegulatedMotor	graber;
	
	/**
	 * Initialise ce graber
	 */
	public Graber(){
		Port port   	= LocalEV3.get().getPort(Main.GRABER);
		this.graber 	= new EV3LargeRegulatedMotor(port);
		
		this.graber.setSpeed(Main.GRABER_SPEED);
	}
	
	/**
	 * Ferme les pinces
	 */
	synchronized public void close(){
		while(this.graber.isMoving()){
			Thread.yield();
		}
		if(Main.HAND_OPEN){
			this.graber.rotate((-1)*Main.GRABER_TIMER, true);
			Main.HAND_OPEN = false;
		}
	}
	
	/**
	 * Ouvre les pinces
	 */
	synchronized public void open(){
		while(this.graber.isMoving()){
			Thread.yield();
		}
		if(!Main.HAND_OPEN){
			this.graber.rotate(Main.GRABER_TIMER/2, false);
			this.graber.rotate(Main.GRABER_TIMER/2, true);
			Main.HAND_OPEN = true;
		}
	}
	
	/**
	 * Arrête le grab immédiatement, la position des pinces peut être inconsistante après.
	 */
	public void stop() {
		this.graber.stop();
	}

}
