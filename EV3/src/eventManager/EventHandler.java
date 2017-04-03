package eventManager;

import aiPlanner.Main;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import positionManager.VisionSensor;
import shared.SignalType;

public class EventHandler extends Thread implements MoveListener{
	
	private SignalListener		aiPlanner;
	private PressionSensor		pressSensor;
	private SignalType 			currentPression;
	private VisionSensor 		radar;
	
	private boolean 			currentEsc;
	private int					lastPression;
	private int					moveStarted;
	private int					refreshRate			= 300;
	private volatile boolean	checkWall			= false;
	
	private static final int	NO_MOVE 			= 9999;
	private static final int	MAX_TIME_STALLED	= 12;
	
	
	public EventHandler(SignalListener marvin, VisionSensor radar){
		this.pressSensor 		= new PressionSensor();
		this.aiPlanner			= marvin;
		this.currentPression	= SignalType.PRESSION_RELEASED;
		this.currentEsc 		= false;
		this.radar				= radar;
		Main.printf("[EVENTHANDLER]          : Initialized");
	}
	
	@Override
	public void run() {
		this.setPriority(MIN_PRIORITY);
		Main.printf("[EVENTHANDLER]          : Started");
		while(! isInterrupted()){
			checkEscPressed();
			checkPression();
			if(this.checkWall){
				checkWall();
			}
			checkInfiniteMove();
			syncWait();
		}
		Main.printf("[EVENTHANDLER]          : Finished");
	}
	
	private void checkEscPressed(){
		if(Button.ESCAPE.isDown() && !this.currentEsc){
			this.currentEsc = true;
			sendSignal(SignalType.STOP);
		}
		this.currentEsc = false;
	}
	
	private void checkWall(){
		if(this.radar.getRadarDistance() < Main.RADAR_WALL_DETECT){
			sendSignal(SignalType.OBSTACLE);
		}
	}
	
	private void checkPression(){
		if(this.currentPression == SignalType.PRESSION_PUSHED && (this.pressSensor.isPressed() == false) && (Main.TIMER.getElapsedSec() - this.lastPression > 2)){
			this.currentPression = SignalType.PRESSION_RELEASED;
			sendSignal(SignalType.PRESSION_RELEASED);
			Main.setState(Main.PRESSION, false);
		}
		else{
			if(this.currentPression == SignalType.PRESSION_RELEASED && this.pressSensor.isPressed()){
				Main.printf("PRESSION HANDLER = " + Main.getState(Main.PRESSION));
				this.currentPression = SignalType.PRESSION_PUSHED;
				Main.setState(Main.PRESSION, true);
				sendSignal(SignalType.PRESSION_PUSHED);
				this.lastPression = Main.TIMER.getElapsedSec();
				
			}
		}
	}
	
	private void checkInfiniteMove(){
		if(Main.TIMER.getElapsedSec() - this.moveStarted > MAX_TIME_STALLED && Main.getState(Main.HAS_MOVED)){
			sendSignal(SignalType.STALLED_ENGINE);
		}
	}
	
	synchronized private void syncWait(){
		try {
			this.wait(this.refreshRate);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	synchronized public void researchMode(boolean isSearching){
		if(isSearching){
			this.setPriority(NORM_PRIORITY);
			this.refreshRate = 100;
		}
		else{
			this.setPriority(MIN_PRIORITY);
			this.refreshRate = 300;
		}
		this.notifyAll();
	}
	
	public void setCheckWall(boolean b){
		this.checkWall = b;
	}

	public void moveStarted(Move event, MoveProvider mp) {
		Main.printf("[EVENTHANDLER]          : Move Started");
		this.moveStarted = Main.TIMER.getElapsedSec();
		
	}

	public void moveStopped(Move event, MoveProvider mp) {
		Main.printf("[EVENTHANDLER]          : Move Ended");
		this.moveStarted = NO_MOVE;
	}
	
	private void sendSignal(SignalType s){
		this.aiPlanner.signal(s);
	}
}
