package motorsManager;

import aiPlanner.Main;
import interfaces.ModeListener;
import shared.Mode;

public class GraberManager extends Thread implements ModeListener{
	
	private Graber graber;
	private Action askedAction;
	private Action currentState;
	private volatile Mode currentMode;
	private int refreshRate;
	
	public GraberManager(){
		graber = new Graber();
		askedAction = Action.NOTHING;
		if(Main.getState(Main.HAND_OPEN)){
			currentState = Action.OPEN;
		}
		else{
			currentState = Action.CLOSE;
		}
		setMode(Mode.ACTIVE);
		Main.printf("[GRABER]                : Initialized");
	}
	
	public void run(){
		Main.printf("[GRABER]                : Started");
		while(! isInterrupted() && currentMode != Mode.END){
			if(askedAction == Action.CLOSE && currentState == Action.OPEN){
				graber.close();
				currentState = Action.CLOSE;
				askedAction = Action.NOTHING;
				Main.setState(Main.HAND_OPEN,false);
			}
			else if(askedAction == Action.OPEN && currentState == Action.CLOSE){
				graber.open();
				currentState = Action.OPEN;
				askedAction = Action.NOTHING;
				Main.setState(Main.HAND_OPEN,true);
			}
			else{
				askedAction = Action.NOTHING;
			}
			syncWait();
		}
		Main.printf("[GRABER]                : Finished");
	}
	
	synchronized public boolean close(){
		Main.printf(askedAction.toString());
		Main.printf(currentState.toString());
		if(askedAction == Action.NOTHING && currentState == Action.OPEN){
			askedAction = Action.CLOSE;
			return true;
		}
		return false;
	}
	
	synchronized public boolean open(){
		if(askedAction == Action.NOTHING && currentState == Action.CLOSE){
			askedAction = Action.OPEN;
			return true;
		}
		return false;
	}
	
	synchronized public void stopGrab(){
		graber.stop();
	}
	
	public void syncWait(){
		synchronized (this) {
			try {
				this.wait(refreshRate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public Action getCurrentState(){
		return currentState;
	}

	public void setMode(Mode m) {
		this.currentMode = m;
		switch (m){
			case ACTIVE:
				refreshRate = 200;
				break;
			default:
				refreshRate = 500;
				break;
		}
	}
}
