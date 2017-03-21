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
	protected Boolean 		restartOnInterrupt	= false; // a utiliser avec prudence

	public GoalGoToPosition(Marvin ia, int timeout, Point p, OrderType backward, PoseGiver pg, boolean restartOnInterrupt) {
		super(ia, timeout);
		this.destinationPoint = p;
		this.pg = pg;
		this.backward = backward;
		this.restartOnInterrupt = restartOnInterrupt;
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.CALIBRATED);
		
		postConditions.add(Main.HAS_MOVED);
	}

	@Override
	public void start() {
		Pose currentPose = pg.getPosition();

		int angle = (int) currentPose.relativeBearing(destinationPoint);
		Main.printf("ANGLE A corriger = " + angle);
		
		int distance = (int) currentPose.distanceTo(destinationPoint);
		Main.printf("DISTANCE = " + distance);
		
		// si on doit aller en marche arrière ou si c'est plus rapide
		if(backward == OrderType.MANDATORY || (Math.abs(angle) > 90 && backward != OrderType.FORBIDEN)){
			ia.turnHere(angle + 180,Main.ROTATION_SPEED);
			ia.goBackward(distance,Main.CRUISE_SPEED);
		}
		else{
			ia.turnHere(angle,Main.ROTATION_SPEED);
			ia.goForward(distance,Main.CRUISE_SPEED);
		}
		
		// si on a été interrompu et qu'on veut quand même finir
		if (restartOnInterrupt && (pg.getPosition().distanceTo(destinationPoint)) > 100 && (distance > 100)){
			ia.pushGoal(this);
		}
	}

	@Override
	public void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getName() {
		return NAME;
	}

}
