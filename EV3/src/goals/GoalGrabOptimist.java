package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

public class GoalGrabOptimist extends GoalGrabPessimist {

	public GoalGrabOptimist(GoalFactory gf, Marvin ia, int timeout, Point pallet, PoseGiver pg, ItemGiver eom) {
		super(gf, ia, timeout, pallet, pg, eom);
	}
	
	@Override
	public void start() {
		if(eom.checkPallet(new IntPoint(pallet.x, pallet.y))){
			correctPosition();
	
			int	radarDistance 	= pg.getRadarDistance();
			Pose currentPose 	= pg.getPosition();
			int distance 		= (int)currentPose.distanceTo(pallet);
			
			ia.setAllowInterrupt(true);
			
			if(radarDistance < Main.RADAR_MAX_RANGE && Main.areApproximatlyEqual(radarDistance,distance,700) ){
				ia.goForward(distance+100);
				if(tryGrab()){
					failGrabHandler();
				}
			}
		}
	}
}
