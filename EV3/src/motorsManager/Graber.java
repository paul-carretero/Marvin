package motorsManager;

import aiPlanner.Main;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

/**
 * Fournit des primitives pour les actions du graber (grab, fermer et stop).
 * Ne considère pas l'état du graber.
 * @see GraberManager
 */
public class Graber {
	/**
	 * Représentation LeJos du Graber
	 */
	private EV3LargeRegulatedMotor	graber;
	
	/**
	 * le Thread gérant le grabber, permet d'attendre sur ce moniteur
	 */
	private WaitProvider			manager;
	
	/**
	 * @param manager le Thread gérant le grabber, permet d'attendre sur ce moniteur
	 */
	public Graber(WaitProvider manager){
		Port port   	= LocalEV3.get().getPort(Main.GRABER);
		this.graber 	= new EV3LargeRegulatedMotor(port);
		this.manager	= manager;
		
		this.graber.setSpeed(Main.GRABER_SPEED);
	}
	
	/**
	 * Ferme les pinces
	 */
	public void close(){
		this.graber.backward();
		this.manager.syncWait(Main.GRABER_TIMER);
		this.graber.stop();
	}
	
	/**
	 * Ouvre les pinces
	 */
	public void open(){
		this.graber.forward();
		this.manager.syncWait(Main.GRABER_TIMER);
		this.graber.stop();
	}
	
	/**
	 * Arrête le grab immédiatement, la position des pinces peut être inconsistante après.
	 */
	public void stop() {
		this.graber.stop();
	}

}
