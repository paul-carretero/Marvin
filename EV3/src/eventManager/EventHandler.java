package eventManager;

import aiPlanner.Main;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import positionManager.VisionSensor;

/**
 * Classe permettant de gérer les évenements et interruption exterieurs au système
 * Permet également de detecter certain type d'erreurs
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
	private VisionSensor 		radar;
	
	private boolean 			currentEsc;
	private int					lastPression;
	private int					lastWall;
	private int					moveStarted;
	
	private static final int	NO_DATA 			= 9999;
	private static final int	MAX_TIME_STALLED	= 12;
	private static final int	PRESSION_DELAY		= 2;
	private static final int	WALL_DELAY			= 4;
	private static final int	REFRESH_RATE		= 120;
	
	
	public EventHandler(SignalListener marvin, VisionSensor radar){
		this.pressSensor 		= new PressionSensor();
		
		this.aiPlanner			= marvin;
		this.currentPression	= false;
		this.currentEsc 		= false;
		this.radar				= radar;
		
		this.moveStarted		= NO_DATA;
		this.lastPression		= -1;
		this.lastWall			= -1;
		
		Main.printf("[EVENTHANDLER]          : Initialized");
	}
	
	@Override
	public void run() {
		Main.printf("[EVENTHANDLER]          : Started");
		this.setPriority(MIN_PRIORITY);
		
		while(!isInterrupted()){
			
			checkPression();
			checkEscPressed();
			checkInfiniteMove();
			checkWall();
			
			syncWait();
		}
		
		Main.printf("[EVENTHANDLER]          : Finished");
	}
	
	private void checkEscPressed(){
		if(Button.ESCAPE.isDown() && !this.currentEsc){
			this.currentEsc = true;
			this.aiPlanner.signalStop();
		}
		this.currentEsc = false;
	}
	
	private void checkWall(){
		if(this.radar.getRadarDistance() < Main.RADAR_WALL_DETECT && this.radar.getRadarDistance() > 0 && (Main.TIMER.getElapsedSec() - this.lastWall) > WALL_DELAY){
			this.aiPlanner.signalObstacle();
			this.lastWall = Main.TIMER.getElapsedSec();
		}
	}
	
	private void checkPression(){
		if(this.currentPression && (this.pressSensor.isPressed() == false) && (Main.TIMER.getElapsedSec() - this.lastPression > PRESSION_DELAY)){
			
			this.currentPression = false;
			Main.PRESSION 		 = false;
			
		}
		else{
			if(!this.currentPression && this.pressSensor.isPressed()){

				this.currentPression = true;
				Main.PRESSION		 = true;
				this.aiPlanner.signalPression();
				this.lastPression	 = Main.TIMER.getElapsedSec();
				
			}
		}
	}
	
	private void checkInfiniteMove(){
		if(Main.TIMER.getElapsedSec() - this.moveStarted > MAX_TIME_STALLED && Main.HAS_MOVED){
			this.aiPlanner.signalStalled();
		}
	}
	
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
