package motorsManager;

import java.util.LinkedList;
import java.util.Queue;

import aiPlanner.Main;

public class GraberManager extends Thread{
	
	private final Graber graber;
	private final Queue<Action> actionList;
	
	public GraberManager(){
		this.graber		= new Graber();
		this.actionList = new LinkedList<Action>();
		Main.printf("[GRABER]                : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[GRABER]                : Started");
		while(! isInterrupted()){
			synchronized(this){
				while(!this.actionList.isEmpty()){
					Action todo = this.actionList.poll();
					if(todo == Action.CLOSE && Main.getState(Main.HAND_OPEN)){
						Main.setState(Main.HAND_OPEN,false);
						this.graber.close();
					}
					else if(todo == Action.OPEN && !Main.getState(Main.HAND_OPEN)){
						Main.setState(Main.HAND_OPEN,true);
						this.graber.open();
					}
				}
			}
			syncWait();
		}
		Main.printf("[GRABER]                : Finished");
	}
	
	public void close(){
		synchronized(this){
			this.actionList.add(Action.CLOSE);
			this.notifyAll();
		}
	}
	
	synchronized public void open(){
		this.actionList.add(Action.OPEN);
		this.notifyAll();
	}
	
	synchronized public void stopGrab(){
		this.graber.stop();
		this.actionList.clear();
		this.notifyAll();
	}
	
	synchronized private void syncWait(){
		try {
			this.wait(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
