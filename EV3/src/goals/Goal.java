package goals;

import java.util.ArrayList;

import aiPlanner.Main;
import aiPlanner.Marvin;
import shared.Mode;

public abstract class Goal {
	protected 	ArrayList<Integer>	preConditions;
	protected 	ArrayList<Integer>	postConditions;
	protected 	Mode 				runningMode;
	protected	int 				timeout;
	protected	Marvin 				ia;
	
	public enum OrderType {
		ALLOWED,
		MANDATORY,
		FORBIDEN;
	}
	
	protected Goal(Marvin ia) {
		this.preConditions = new ArrayList<Integer>();
		this.postConditions = new ArrayList<Integer>();
		this.setRunningMode(Mode.ACTIVE);
		this.setTimeout(500000);
		this.ia = ia;
		defineDefault();
	}
	
	protected Goal(Marvin ia, int timeout) {
		this.preConditions = new ArrayList<Integer>();
		this.postConditions = new ArrayList<Integer>();
		this.setRunningMode(Mode.ACTIVE);
		this.setTimeout(timeout);
		this.ia = ia;
		defineDefault();
	}
	
	protected abstract void defineDefault();
	
	public abstract String getName();

	protected Mode getRunningMode() {
		return runningMode;
	}

	protected void setRunningMode(Mode runningMode) {
		this.runningMode = runningMode;
	}
	
	protected ArrayList<Integer> getPrecondition(){
		return preConditions;
	}
	
	protected ArrayList<Integer> getPostCondition(){
		return postConditions;
	}

	protected boolean checkPreConditions(){
		boolean res = true;
		for(int e : preConditions){
			res = res && Main.GLOBALSTATE[e];
		}
		return res;
	}
	
	protected void setPostConditions(){
		for(int e : postConditions){
			Main.setState(e, true);			
		}
	}

	protected int getTimeout() {
		return timeout;
	}

	protected void setTimeout(int timeout) {
		this.timeout = (Main.TIMER.getElapsedMs() + timeout);
	}
	
	protected boolean timeOverCheck(){
		return (this.timeout < Main.TIMER.getElapsedMs()); // si le timeout est avant
	}
	
	protected void syncWait(int ms){
		synchronized (this) {
			try {
				this.wait(ms);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void startWrapper(){
		if(!timeOverCheck()){
			if(this.checkPreConditions()){
				ia.updateMode(this.getRunningMode());
				Main.printf("EXECUTE GOAL : " + getName().toUpperCase());
				this.start();
				this.setPostConditions();
				ia.updateMode(Mode.ACTIVE);
			}
			else{
				Main.printf("FAIL EXECUTE GOAL : " + getName().toUpperCase());
				this.preConditionsFailHandler();
			}
		}
		// else on ne fait rien , l'ia passera à l'objectif suivant.
	}
	
	protected abstract void preConditionsFailHandler();

	protected abstract void start();
	
	protected boolean genericSolver(){
		for(int e : preConditions){
			if(!Main.GLOBALSTATE[e]){
				switch (e) {
				case (Main.CALIBRATED):
					ia.pushGoal(new GoalRecalibrate(ia));
					return true;
				case (Main.HAND_OPEN):
					ia.open();
					return true;
				default:
					return false;
				}
			}
		}
		return true;
	}
}
