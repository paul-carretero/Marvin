package goals;

import java.util.ArrayDeque;
import java.util.Deque;

import aiPlanner.Marvin;
import goals.Goal.OrderType;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;

public class GoalFactory {
	
	private	Marvin		ia;
	private	PoseGiver	pg;
	
	public GoalFactory(Marvin ia, PoseGiver pg){
		this.ia = ia;
		this.pg = pg;
	}
	
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		goals.push(new GoalGrabPessimist(ia, 30000, new Point(500,2100), pg));
		//goals.push(new GoalGoToPosition(ia,10000,new Point(100,50),OrderType.MANDATORY,pg,false));
		return goals;
	}
	
	public Goal play(int timeout){
		return new GoalPlay(ia, timeout);
	}
}
