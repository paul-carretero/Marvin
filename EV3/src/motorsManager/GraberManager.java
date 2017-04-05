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
		this.setPriority(Thread.NORM_PRIORITY);
		while(! isInterrupted()){
			synchronized(this){
				while(!this.actionList.isEmpty()){
					Action todo = this.actionList.poll();
					if(todo == Action.CLOSE && Main.HAND_OPEN){
						Main.HAND_OPEN = false;
						this.graber.close();
					}
					else if(todo == Action.OPEN && !Main.HAND_OPEN){
						Main.HAND_OPEN = true;
						this.graber.open();
					}
				}
			}
			syncWait();
		}
		Main.printf("[GRABER]                : Finished");
	}
	
	synchronized public void close(){
			if(this.actionList.isEmpty()){
				this.actionList.add(Action.CLOSE);
				this.notify();
			}
			else{
				this.actionList.add(Action.CLOSE);
			}
	}
	
	synchronized public void open(){
		if(this.actionList.isEmpty()){
			this.actionList.add(Action.OPEN);
			this.notify();
		}
		else{
			this.actionList.add(Action.OPEN);
		}
	}
	
	synchronized public void stopGrab(){
		this.graber.stop();
		this.actionList.clear();
		this.notify();
	}
	
	synchronized private void syncWait(){
		try {
			this.wait(0);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
