package eventManager;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ModeListener;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;

import shared.Mode;
import shared.SignalType;

public class EventHandler extends Thread implements ModeListener, MoveListener{
	
	private SignalListener	aiPlanner;
	private int 			refreshRate;
	private PressionSensor	pressSensor;
	private SignalType 		currentPression;
	private boolean 		currentEsc;
	
	
	public EventHandler(Marvin marvin){
		pressSensor 	= new PressionSensor();
		aiPlanner		= marvin;
		currentPression	= SignalType.PRESSION_RELEASED;
		currentEsc 		= false;
		setMode(Mode.ACTIVE);
		Main.printf("[EVENTHANDLER]          : Initialized");
	}
	
	@Override
	public void run() {
		Main.printf("[EVENTHANDLER]          : Started");
		while(! isInterrupted()){
			checkEscPressed();
			checkPression();
			syncWait();
		}
		Main.printf("[EVENTHANDLER]          : Finished");
	}
	
	private void checkEscPressed(){
		if(Button.ESCAPE.isDown() && !currentEsc){
			currentEsc = true;
			sendSignal(SignalType.STOP);
		}
		currentEsc = false;
	}
	
	private void checkPression(){
		if((currentPression == SignalType.PRESSION_PUSHED) && (pressSensor.isPressed() == false)){
			currentPression = SignalType.PRESSION_RELEASED;
			sendSignal(SignalType.PRESSION_RELEASED);
			
		}
		else{
			if((currentPression == SignalType.PRESSION_RELEASED) && (pressSensor.isPressed())){
				Main.printf("PRESSION HANDLER = " + Main.getState(Main.PRESSION));
				currentPression = SignalType.PRESSION_PUSHED;
				sendSignal(SignalType.PRESSION_PUSHED);
				Main.setState(Main.PRESSION, true);
			}
		}
	}
	
	private void syncWait(){
		synchronized (this) {
			try {
				this.wait(refreshRate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void moveStarted(Move event, MoveProvider mp) {
		Main.printf("[EVENTHANDLER]          : Move Started");
	}

	public void moveStopped(Move event, MoveProvider mp) {
		Main.printf("[EVENTHANDLER]          : Move Ended");
	}
	
	public void sendSignal(SignalType s){
		aiPlanner.signal(s);
	}
	
	public void setMode(Mode m){
		switch (m){
		  case ACTIVE:
			  refreshRate = 300;
			  break;
		  case END:
			  refreshRate = 10; // on termine
			  break;
		  default:
			  refreshRate = 1000;
		}
	}
}
