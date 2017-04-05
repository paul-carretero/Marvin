package goals;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import aiPlanner.Marvin;

public abstract class Goal {
	
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
	}
	
	public abstract GoalType getName();

	@SuppressWarnings("static-method")
	protected boolean checkPreConditions(){
		return true;
	}
	
	public void startWrapper(){
		if(this.checkPreConditions()){
			Main.printf("EXECUTE GOAL : " + getName());
			this.start();
		}
		else{
			Main.printf("FAIL EXECUTE GOAL : " + getName());
		}
	}
	
	protected abstract void start();
}
