package goals;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import aiPlanner.Marvin;

public abstract class Goal {
	
	private final GoalType NAME = GoalType.DEFAULT;
	
	protected 	List<Integer>	preConditions;
	protected 	List<Integer>	postConditions;
	protected	Marvin 				ia;
	protected	GoalFactory			gf;
	
	public enum OrderType {
		ALLOWED,
		MANDATORY,
		FORBIDEN;
	}
	
	protected Goal(GoalFactory gf, Marvin ia) {
		this.preConditions 	= new ArrayList<Integer>();
		this.postConditions	= new ArrayList<Integer>();
		this.ia				= ia;
		this.gf				= gf;
		defineDefault();
	}
	
	protected void defineDefault(){
		/*
		 * Void
		 */
	}
	
	public GoalType getName(){
		return this.NAME;
	}

	protected boolean checkPreConditions(){
		boolean res = true;
		for(int e : this.preConditions){
			
			if(e == Main.HAND_OPEN && !Main.getState(Main.HAND_OPEN)){
				this.ia.open();
			}
			
			res = res && Main.GLOBALSTATE[e];
		}
		return res;
	}
	
	protected void setPostConditions(){
		for(int e : this.postConditions){
			Main.setState(e, true);			
		}
	}
	
	public void startWrapper(){
		if(this.checkPreConditions()){
			Main.printf("EXECUTE GOAL : " + getName());
			this.start();
			this.setPostConditions();
		}
		else{
			Main.printf("FAIL EXECUTE GOAL : " + getName());
		}
	}
	
	protected abstract void start();
}
