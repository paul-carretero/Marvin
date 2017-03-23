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
	protected	GoalFactory			gf;
	
	public enum OrderType {
		ALLOWED,
		MANDATORY,
		FORBIDEN;
	}
	
	protected Goal(GoalFactory gf, Marvin ia, int timeout) {
		this.preConditions 	= new ArrayList<Integer>();
		this.postConditions	= new ArrayList<Integer>();
		this.ia				= ia;
		this.gf				= gf;
		this.setRunningMode(Mode.ACTIVE);
		this.setTimeout(timeout);
		defineDefault();
	}
	
	protected boolean checkTimeout(){
		return timeout > Main.TIMER.getElapsedMs();
	}
	
	protected abstract void defineDefault();
	
	public abstract String getName();

	protected Mode getRunningMode() {
		return runningMode;
	}

	protected void setRunningMode(Mode runningMode) {
		this.runningMode = runningMode;
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
			if(this.checkPreConditions() && this.checkTimeout()){
				ia.updateMode(this.getRunningMode());
				Main.printf("EXECUTE GOAL : " + getName().toUpperCase());
				this.start();
				this.setPostConditions();
				ia.updateMode(Mode.ACTIVE);
			}
			else{
				Main.printf("FAIL EXECUTE GOAL : " + getName().toUpperCase());
			}
		}
		// else on ne fait rien , l'ia passera à l'objectif suivant.
	}
	
	protected abstract void start();
}
