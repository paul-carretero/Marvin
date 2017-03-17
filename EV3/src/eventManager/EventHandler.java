package eventManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.ModeListener;
import interfaces.SignalListener;
import lejos.hardware.Button;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;

import shared.Mode;
import shared.SignalType;

public class EventHandler extends Thread implements ModeListener, MoveListener, SignalListener{
	private List<SignalListener> signalListeners;
	private int refreshRate;
	private PressionSensor pressSensor;
	
	private SignalType currentPression;
	private boolean currentEsc;
	
	
	public EventHandler(){
		pressSensor 	= new PressionSensor();
		signalListeners = new ArrayList<SignalListener>();
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
			broadcast(SignalType.STOP);
		}
		currentEsc = false;
	}
	
	private void checkPression(){
		if((currentPression == SignalType.PRESSION_PUSHED) && (pressSensor.isPressed() == false)){
			currentPression = SignalType.PRESSION_RELEASED;
			broadcast(SignalType.PRESSION_RELEASED);
		}
		else{
			if((currentPression == SignalType.PRESSION_RELEASED) && (pressSensor.isPressed())){
				currentPression = SignalType.PRESSION_PUSHED;
				broadcast(SignalType.PRESSION_PUSHED);
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

	public void addSignalListener(SignalListener l) {
		this.signalListeners.add(l);
	}
	
	public void broadcast(SignalType s){
		for (SignalListener l : signalListeners) {
			l.signal(s);
		}
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

	public void signal(SignalType e) {
		// TODO Auto-generated method stub
		// doit recevoir le signal lost par exemple
	}
}
