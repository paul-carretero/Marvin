package goals;

import aiPlanner.Marvin;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import shared.IntPoint;

public class GoalGrabOptimist extends GoalGrabPessimist {
	
	protected final GoalType		NAME = GoalType.GRAB_OPTIMISTE;

	public GoalGrabOptimist(GoalFactory gf, Marvin ia, Point palet, PoseGiver pg, ItemGiver eom, DistanceGiver radar) {
		super(gf, ia, palet, pg, eom, radar);
	}
	
	@Override
	public void start() {
		
		if(this.eom.checkpalet(new IntPoint(this.palet))){
			
			correctPosition();
			grabWrapper();
			
		}
		
		updateStatus();
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
