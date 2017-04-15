package goals;

import java.util.LinkedList;
import java.util.List;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Color;
import shared.Couple;
import shared.IntPoint;

public class GoalRecalibrate extends Goal {
	
	protected final GoalType NAME = GoalType.RECALIBRATE;
	
	private ItemGiver 		eom;
	private PoseGiver 		pg;

	/**
	 * @param gf
	 * @param ia
	 * @param eom
	 * @param pg
	 */
	public GoalRecalibrate(GoalFactory gf, Marvin ia, ItemGiver eom, PoseGiver pg) {
		super(gf, ia);
		this.eom	= eom;
		this.pg		= pg;
		// TODO Auto-generated constructor stub
	}

	@Override
	// si ligne blanche = demi tour
	public void start() {
		this.ia.addMeWakeUpOnColor();
		
		this.ia.setSpeed(Main.RESEARCH_SPEED);
		
		this.ia.goForward(2000);
		this.ia.goBackward(5);
		
		Color color = this.ia.getColor();
		
		if(color == Color.WHITE){
			this.ia.turnHere(180);
			this.ia.pushGoal(this.gf.goalRecalibrate());
		}
		else if(color == Color.GREY || color == Color.BLACK){
			this.ia.pushGoal(this.gf.goalRecalibrate());
		}
		else{
			
			List<IntPoint> initialList = this.eom.searchPosition(color);
			
			this.ia.turnHere(180);
			
			this.ia.goForward(300);
			
			List<IntPoint> finalList = this.eom.searchPosition(color);
			
			IntPoint start	= null;
			boolean error	= false;
			
			for(IntPoint p : initialList){
				if(!finalList.contains(p)){
					if(start != null){
						error = true;
					}
					start = p;
				}
			}
			
			// si il n'y en a eu qu'un qui est supprimé de la ligne
			if(!error && start != null){
				List<IntPoint> resList = this.eom.searchPosition(start, 200, 400);
				
				if(resList.size() == 1){
					IntPoint me = resList.get(0);
					float angle = getAngle(start.toLejosPoint(), me.toLejosPoint());
					
					Pose myPose = new Pose(me.x(), me.y(), angle);
					
					this.pg.setPose(myPose);
					Main.printf("calculated pose = " + myPose);
				}
			}
			
		}
		
		this.ia.setSpeed(Main.CRUISE_SPEED);
		this.ia.removeMeWakeUpOnColor();
	}
	
	public static float getAngle(Point start, Point end){
		if(start != null && end != null){
			return start.angleTo(end);
		}
		return 0;
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
