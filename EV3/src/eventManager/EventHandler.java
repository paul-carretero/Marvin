package eventManager;

import aiPlanner.Main;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import positionManager.VisionSensor;

/**
 * Classe permettant de g�rer les �venements et interruption exterieurs au syst�me.
 * Permet �galement de detecter certain type d'erreurs.
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
	 * vrai si la derni�re pression enregistr� est push, faux sinon
	 */
	private boolean 			currentPression;
	
	/**
	 * Fournit des primitives pour l'utilisation du radar du robot
	 */
	private VisionSensor 		radar;
	
	/**
	 * vrai si la touche echap est appuy�e, faux sinon
	 */
	private boolean 			currentEsc;
	
	/**
	 * Temps de la derni�re detection de pression (d'un palet)
	 */
	private int					lastPression;
	
	/**
	 * Temps de la derni�re detection d'un mur
	 */
	private int					lastWall;
	
	/**
	 * Temps ou le mouvement a commenc�
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
	 * Temps minimum en seconde avant que l'on consid�re que l'on a plus de pression palet (depuis la derni�re v�rification positive)
	 * Temps minimum en seconde avant de v�rifier � nouveau si l'on est bloqu� par un obstacle
	 */
	private static final int	SENSOR_DELAY		= 2;
	
	/**
	 * Temps entre chaque cycle de v�rifications
	 */
	private static final int	REFRESH_RATE		= 120;
	
	
	/**
	 * @param marvin ia permettant de traiter les signaux g�nr�s
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
	 * V�rifie a intervals constant diff�rents �venement pouvant survenir de mani�re "non d�cid�" par le robot.<br>
	 * D�tecte notament les obstacles et lorsqu'un palet est touch�.
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
	 * V�rifie si la touche echap est appuy�e, si oui alors g�n�re un signal � l'ia
	 */
	private void checkEscPressed(){
		if(Button.ESCAPE.isDown() && !this.currentEsc){
			this.currentEsc = true;
			this.aiPlanner.signalStop();
		}
		this.currentEsc = false;
	}
	
	/**
	 * V�rifie si le robot est face � un mur, si oui alors g�n�re un signal � l'ia (ne peut pas se d�clanch� en continu)
	 */
	private void checkWall(){
		if(this.radar.getRadarDistance() < Main.RADAR_WALL_DETECT && this.radar.getRadarDistance() > 0 && (Main.TIMER.getElapsedSec() - this.lastWall) > SENSOR_DELAY){
			this.aiPlanner.signalStalled();
			this.lastWall = Main.TIMER.getElapsedSec();
		}
	}
	
	/**
	 * V�rifie la pression de capteur de pression.
	 * Informe l'ia si une pression est d�tect�e et met � jour les variable d'�tat (Main)
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
	 * V�rifie si un mouvement semble s'�x�cuter pendant une dur�e infinie
	 * Informe l'ia si oui
	 */
	private void checkInfiniteMove(){
		if(this.moveStarted != NO_DATA && Main.TIMER.getElapsedSec() - this.moveStarted > MAX_TIME_STALLED){
			this.aiPlanner.signalStalled();
		}
	}
	
	/**
	 * Attends de mani�re synchronis� pensant la dur�e REFRESH_RATE
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
