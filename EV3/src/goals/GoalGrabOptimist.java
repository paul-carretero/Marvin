package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

public class GoalGrabOptimist extends GoalGrabPessimist {
	
	protected final GoalType		NAME = GoalType.GRAB_OPTIMISTE;

	public GoalGrabOptimist(GoalFactory gf, Marvin ia, Point pallet, PoseGiver pg, ItemGiver eom, DistanceGiver radar) {
		super(gf, ia, pallet, pg, eom, radar);
	}
	
	@Override
	public void start() {
		this.ia.setResearchMode(true);
		if(this.eom.checkPallet(new IntPoint(this.pallet))){
			correctPosition();
			
			int	radarDistance 	= this.radar.getRadarDistance();
			Pose currentPose 	= this.pg.getPosition();
			int distance 		= (int)currentPose.distanceTo(this.pallet);
			
			this.ia.setAllowInterrupt(true);
			
			Main.printf("radarDistance = " + radarDistance);

			this.ia.goForward(distance);
			
			if(!tryGrab()){
				failGrabHandler();
			}
			
			this.ia.setAllowInterrupt(false);
		}
		this.ia.setResearchMode(false);
		updateStatus();
	}
}
