package motorsManager;

import java.util.LinkedList;
import java.util.Queue;

import aiPlanner.Main;
import interfaces.ModeListener;
import shared.Mode;

public class GraberManager extends Thread implements ModeListener{
	
	private Graber graber;
	private Queue<Action> actionList;
	private Mode currentMode;
	
	public GraberManager(){
		graber = new Graber();
		actionList = new LinkedList<Action>();
		setMode(Mode.ACTIVE);
		Main.printf("[GRABER]                : Initialized");
	}
	
	public void run(){
		Main.printf("[GRABER]                : Started");
		while(! isInterrupted() && currentMode != Mode.END){
			synchronized(this){
				while(!actionList.isEmpty()){
					Action todo = actionList.poll();
					if(todo == Action.CLOSE && Main.getState(Main.HAND_OPEN)){
						graber.close();
						Main.setState(Main.HAND_OPEN,false);
					}
					else if(todo == Action.OPEN && !Main.getState(Main.HAND_OPEN)){
						graber.open();
						Main.setState(Main.HAND_OPEN,true);
					}
				}
			}
			syncWait();
		}
		Main.printf("[GRABER]                : Finished");
	}
	
	public void close(){
		synchronized(this){
			actionList.add(Action.CLOSE);
			this.notify();
		}
	}
	
	public void open(){
		synchronized(this){
			actionList.add(Action.OPEN);
			this.notify();
		}
	}
	
	public void stopGrab(){
		graber.stop();
		synchronized(this){
			actionList.clear();
		}
	}
	
	public void syncWait(){
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void setMode(Mode m) {
		this.currentMode = m;
	}
}
