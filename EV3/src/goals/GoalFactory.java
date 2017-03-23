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
	
	public GoalFactory(Marvin ia, PoseGiver pg, ItemGiver eom){
		this.ia		= ia;
		this.pg		= pg;
		this.eom	= eom;
	}
	
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		//goals.push(goalDrop(30000));
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
}
