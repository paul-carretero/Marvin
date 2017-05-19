package eventManager;

import aiPlanner.Main;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import positionManager.VisionSensor;

/**
 * Classe permettant de gérer les évenements et interruption exterieurs au système.
 * Permet également de detecter certain type d'erreurs.
 * @author paul.carretero, florent.chastagner
 */
public class EventHandler extends Thread implements MoveListener{
	
	/**
	 * Instance du controlleur du robot
	 */
	private SignalListener		aiPlanner;
	
	/**
	 * Instance du capteur de pression
	 */
	private PressionSensor		pressSensor;
	
	/**
	 * vrai si la dernière pression enregistré est push, faux sinon
	 */
	private boolean 			currentPression;
	
	/**
	 * Fournit des primitives pour l'utilisation du radar du robot
	 */
	private VisionSensor 		radar;
	
	/**
	 * vrai si la touche echap est appuyée, faux sinon
	 */
	private boolean 			currentEsc;
	
	/**
	 * Temps de la dernière detection de pression (d'un palet)
	 */
	private int					lastPression;
	
	/**
	 * Temps de la dernière detection d'un mur
	 */
	private int					lastWall;
	
	/**
	 * Temps ou le mouvement a commencé
	 */
	private int					moveStarted;
	
	/**
	 * Symbolise un manque d'information pour movestarted par exemple
	 */
	private static final int	NO_DATA 			= 9999;
	
	/**
	 * Temps maximum en seconde de voyage avant declanchement de la procedure d'annulation d'un mouvement infini
	 */
	private static final int	MAX_TIME_STALLED	= 12;
	
	/**
	 * Temps minimum en seconde avant que l'on considère que l'on a plus de pression palet (depuis la dernière vérification positive)
	 * Temps minimum en seconde avant de vérifier à nouveau si l'on est bloqué par un obstacle
	 */
	private static final int	SENSOR_DELAY		= 2;
	
	/**
	 * Temps entre chaque cycle de vérifications
	 */
	private static final int	REFRESH_RATE		= 120;
	
	
	/**
	 * @param marvin ia permettant de traiter les signaux génrés
	 * @param radar Fournit des primitives pour l'utilisation du radar du robot
	 */
	public EventHandler(SignalListener marvin, VisionSensor radar){
		super("EventHandler");
		this.pressSensor 		= new PressionSensor();
		
		this.aiPlanner			= marvin;
		this.currentPression	= false;
		this.currentEsc 		= false;
		this.radar				= radar;
		
		this.moveStarted		= NO_DATA;
		this.lastPression		= -1;
		this.lastWall			= -1;
		
		Main.printf("[EVENT MANAGER]         : Initialized");
	}
	
	/**
	 * Vérifie a intervals constant différents évenement pouvant survenir de manière "non décidé" par le robot.<br>
	 * Détecte notament les obstacles et lorsqu'un palet est touché.
	 */
	@Override
	public void run() {
		Main.printf("[EVENT MANAGER]         : Started");
		this.setPriority(MIN_PRIORITY);
		
		while(!isInterrupted()){
			
			checkPression();
			checkEscPressed();
			checkInfiniteMove();
			if(Main.USE_RADAR){
				checkWall();
			}

			syncWait();
		}
		
		Main.printf("[EVENT MANAGER]         : Finished");
	}
	
	/**
	 * Vérifie si la touche echap est appuyée, si oui alors génère un signal à l'ia
	 */
	private void checkEscPressed(){
		if(Button.ESCAPE.isDown() && !this.currentEsc){
			this.currentEsc = true;
			this.aiPlanner.signalStop();
		}
		this.currentEsc = false;
	}
	
	/**
	 * Vérifie si le robot est face à un mur, si oui alors génère un signal à l'ia (ne peut pas se déclanché en continu)
	 */
	private void checkWall(){
		if(this.radar.getRadarDistance() < Main.RADAR_WALL_DETECT && this.radar.getRadarDistance() > 0 && (Main.TIMER.getElapsedSec() - this.lastWall) > SENSOR_DELAY){
			this.aiPlanner.signalStalled();
			this.lastWall = Main.TIMER.getElapsedSec();
		}
	}
	
	/**
	 * Vérifie la pression de capteur de pression.
	 * Informe l'ia si une pression est détectée et met à jour les variable d'état (Main)
	 */
	private void checkPression(){
		if(this.currentPression && (this.pressSensor.isPressed() == false) && (Main.TIMER.getElapsedSec() - this.lastPression > SENSOR_DELAY)){
			
			this.currentPression = false;
			Main.PRESSION 		 = false;
			
		}
		else if(this.pressSensor.isPressed()){
			this.currentPression = true;
			Main.PRESSION		 = true;
			this.aiPlanner.signalPression();
			this.lastPression	 = Main.TIMER.getElapsedSec();
				
		}
	}
	
	/**
	 * Vérifie si un mouvement semble s'éxécuter pendant une durée infinie
	 * Informe l'ia si oui
	 */
	private void checkInfiniteMove(){
		if(this.moveStarted != NO_DATA && Main.TIMER.getElapsedSec() - this.moveStarted > MAX_TIME_STALLED){
			this.aiPlanner.signalStalled();
		}
	}
	
	/**
	 * Attends de manière synchronisé pensant la durée REFRESH_RATE
	 */
	synchronized private void syncWait(){
		try {
			this.wait(REFRESH_RATE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void moveStarted(Move event, MoveProvider mp) {
		this.moveStarted = Main.TIMER.getElapsedSec();
	}

	public void moveStopped(Move event, MoveProvider mp) {
		this.moveStarted = NO_DATA;
	}

}
