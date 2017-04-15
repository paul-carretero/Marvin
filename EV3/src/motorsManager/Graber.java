package motorsManager;

import aiPlanner.Main;
import interfaces.WaitProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

/**
 * Fournit des primitives pour les actions du graber (grab, fermer et stop).
 * Ne consid�re pas l'�tat du graber.
 * @see GraberManager
 */
public class Graber {
	/**
	 * Repr�sentation LeJos du Graber
	 */
	private EV3LargeRegulatedMotor	graber;
	
	/**
	 * le Thread g�rant le grabber, permet d'attendre sur ce moniteur
	 */
	private WaitProvider			manager;
	
	/**
	 * @param manager le Thread g�rant le grabber, permet d'attendre sur ce moniteur
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
	 * Arr�te le grab imm�diatement, la position des pinces peut �tre inconsistante apr�s.
	 */
	public void stop() {
		this.graber.stop();
	}

}
