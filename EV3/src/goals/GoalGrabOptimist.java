package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalGrabOptimist extends GoalGrabPessimist {

	public GoalGrabOptimist(Marvin ia, int timeout, Point pallet, PoseGiver pg) {
		super(ia, timeout, pallet, pg);
	}
	
	@Override
	public void start() {
		// faudra aussi vérifier que le pallet est sur la mastertable...
		Main.printf("Here I am, brain the size of a planet, and they ask me to pick up a piece of paper.");
		
		correctPosition();

		int	radarDistance 	= pg.getRadarDistance();
		Pose currentPose 	= pg.getPosition();
		int distance 		= (int)currentPose.distanceTo(pallet);
		
		if(radarDistance < Main.RADAR_MAX_RANGE && Main.areApproximatlyEqual(radarDistance,distance,700) ){
			ia.goForward(distance+100, Main.CRUISE_SPEED);
			if(tryGrab()){
				failGrabHandler();
			}
		}
	}
}
