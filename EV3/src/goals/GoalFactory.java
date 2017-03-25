package goals;

import java.util.ArrayDeque;
import java.util.Deque;

import aiPlanner.Marvin;
import goals.Goal.OrderType;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;

public class GoalFactory {
	
	private	Marvin		ia;
	private	PoseGiver	pg;
	private	ItemGiver	eom;
	private	boolean 	lastGrabOk;
	
	public GoalFactory(Marvin ia, PoseGiver pg, ItemGiver eom){
		this.ia			= ia;
		this.pg			= pg;
		this.eom		= eom;
		this.lastGrabOk = true;
	}
	
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		//goals.push(goalGrabAndDropPalet(30000));
		//goals.push(goalGrabAndDropPalet(30000));
		goals.push(goalGoToPosition(30000, new Point(500,1500), OrderType.FORBIDEN));
		goals.push(goalGoToPosition(30000, new Point(1000,2100), OrderType.MANDATORY));
		return goals;
	}
	
	public Goal play(int timeout){
		return new GoalPlay(this, ia, timeout);
	}
	
	public Goal goalGoToPosition(int timeout, Point destination, OrderType backward){
		return new GoalGoToPosition(this,ia,timeout,destination,backward,pg);
	}
	
	public Goal goalDrop(int timeout){
		return new GoalDrop(this,ia,timeout,pg);
	}
	
	public Goal goalGrab(int timeout, Point palet){
		if(lastGrabOk){
			return new GoalGrabOptimist(this, ia, timeout, palet, pg, eom);
		}
		else{
			return new GoalGrabPessimist(this, ia, timeout, palet, pg, eom);
		}
	}
	
	public Goal goalGrabAndDropPalet(int timeout){
		return new GoalGrabAndDropPalet(this, ia, timeout, eom);
	}
}
