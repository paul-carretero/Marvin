package goals;

import java.util.LinkedList;
import java.util.List;

import aiPlanner.Marvin;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Couple;
import shared.IntPoint;

public class GoalRecalibrate extends Goal {
	
	protected final GoalType NAME = GoalType.RECALIBRATE;
	
	private ItemGiver 		eom;
	private PoseGiver 		pg;

	public GoalRecalibrate(GoalFactory gf, Marvin ia, ItemGiver eom, PoseGiver pg) {
		super(gf, ia);
		this.eom	= eom;
		this.pg		= pg;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start() {
		this.ia.goForward(3000);
		
		List<IntPoint> initialList = this.eom.searchPosition(200);
		
		if(initialList.isEmpty()){
			this.ia.turnHere(180);
			this.ia.pushGoal(this.gf.goalRecalibrate());
		}
		else{

			this.ia.goBackward(800);
			
			List<Couple> couples = new LinkedList<Couple>();
			
			for(IntPoint startPoint : initialList){
				List<IntPoint> finalList = this.eom.searchPosition(startPoint,300, 900);
				
				for(IntPoint finalp : finalList){
					couples.add(new Couple(startPoint,finalp));
				}
				
			}

			if(couples.size() == 1){
				float h = getAngle(couples.get(0).getfirst().toLejosPoint(),couples.get(0).getsecond().toLejosPoint());
				float x = couples.get(0).getsecond().x();
				float y = couples.get(0).getsecond().y();
				
				this.pg.setPose(new Pose(x,y,h));
			}
			else{
				this.ia.turnHere(180);
				this.ia.pushGoal(this.gf.goalRecalibrate());
			}
		}
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
