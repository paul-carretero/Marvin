package goals;

import java.util.ArrayDeque;
import java.util.Deque;

import aiPlanner.Marvin;
import goals.Goal.OrderType;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;

public class GoalFactory {
	
	private	Marvin			ia;
	private	PoseGiver		pg;
	private	ItemGiver		eom;
	private	boolean 		lastGrabOk;
	private	DistanceGiver	radar;
	
	public GoalFactory(Marvin ia, PoseGiver pg, ItemGiver eom, DistanceGiver radar){
		this.ia			= ia;
		this.pg			= pg;
		this.eom		= eom;
		this.lastGrabOk = true;
		this.radar		= radar;
	}
	
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		goals.push(goalGrabAndDropPalet());
		goals.push(goalGrabAndDropPalet());
		//goals.push(goalGoToPosition(30000, new Point(500,1500), OrderType.FORBIDEN));
		//goals.push(goalGoToPosition(30000, new Point(1000,2100), OrderType.MANDATORY));
		return goals;
	}
	
	public void setLastGrab(boolean b){
		this.lastGrabOk = b;
	}
	
	public Goal play(){
		return new GoalPlay(this, this.ia);
	}
	
	public Goal goalGoToPosition(Point destination, OrderType backward){
		return new GoalGoToPosition(this,this.ia,destination,backward,this.pg);
	}
	
	public Goal goalDrop(){
		return new GoalDrop(this,this.ia,this.pg);
	}
	
	public Goal goalGrab(Point palet){
		if(this.lastGrabOk){
			return new GoalGrabOptimist(this, this.ia, palet, this.pg, this.eom, this.radar);
		}
		return new GoalGrabPessimist(this, this.ia, palet, this.pg, this.eom, this.radar);
	}
	
	public Goal goalGrabAndDropPalet(){
		return new GoalGrabAndDropPalet(this, this.ia, this.eom);
	}
	
	public Goal goalRecalibrate(){
		return new GoalRecalibrate(this, this.ia, this.eom, this.pg);
	}
}
