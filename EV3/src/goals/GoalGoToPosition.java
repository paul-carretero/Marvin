package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalGoToPosition extends Goal {
	
	protected final String 	NAME 				= "GoalGoToPosition";
	protected Point 		destinationPoint 	= null;
	protected PoseGiver		pg 					= null;
	protected OrderType 	backward			= null;

	public GoalGoToPosition(GoalFactory gf, Marvin ia, int timeout, Point p, OrderType backward, PoseGiver pg) {
		super(gf, ia, timeout);
		this.destinationPoint = p;
		this.pg = pg;
		this.backward = backward;
	}

	@Override
	protected void defineDefault() {

		postConditions.add(Main.HAS_MOVED);
	}

	@Override
	public void start() {
		Pose currentPose = pg.getPosition();

		int angle = (int) currentPose.relativeBearing(destinationPoint);
		
		int distance = (int) currentPose.distanceTo(destinationPoint);
		
		// si on doit aller en marche arrière ou si c'est plus rapide
		if(backward == OrderType.MANDATORY || (Math.abs(angle) > 90 && backward != OrderType.FORBIDEN)){
			ia.turnHere(angle + 180);
			ia.goBackward(distance);
		}
		else{
			ia.turnHere(angle);
			ia.goForward(distance);
		}
	}
	
	@Override
	public String getName() {
		return NAME;
	}

}
