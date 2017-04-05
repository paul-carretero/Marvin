package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalGoToPosition extends Goal {
	
	protected final GoalType NAME = GoalType.GO_TO_POSITION;
	
	protected Point 		destinationPoint;
	protected PoseGiver		pg;
	protected OrderType 	backward;

	public GoalGoToPosition(GoalFactory gf, Marvin ia, Point p, OrderType backward, PoseGiver pg) {
		super(gf, ia);
		this.destinationPoint = p;
		this.pg = pg;
		this.backward = backward;
	}

	@Override
	public void start() {
		Pose currentPose = this.pg.getPosition();

		int angle = (int) currentPose.relativeBearing(this.destinationPoint);
		
		int distance = (int) currentPose.distanceTo(this.destinationPoint);
		
		//System.out.println("angle = " + angle);
		//System.out.println("distance = " + distance);
		
		// si on doit aller en marche arrière ou si c'est plus rapide
		if(this.backward == OrderType.MANDATORY || (Math.abs(angle) > 90 && this.backward != OrderType.FORBIDEN)){
			this.ia.turnHere(angle + 180);
			this.ia.goBackward(distance);
		}
		else{
			this.ia.turnHere(angle);
			this.ia.goForward(distance);
		}
		
		Main.HAS_MOVED = true;
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
