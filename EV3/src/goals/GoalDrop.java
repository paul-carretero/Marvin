package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalDrop extends Goal{
	
	protected final GoalType NAME = GoalType.DROP;
	
	protected	PoseGiver	poseGiver;
	protected	boolean		restart;

	public GoalDrop(GoalFactory gf, Marvin ia, PoseGiver pg) {
		super(gf, ia);
		this.poseGiver = pg;
	}

	@Override
	protected boolean checkPreConditions() {
		return Main.HAVE_PALET;
	}

	@SuppressWarnings("unused")
	@Override
	protected void start() {
		
		Pose currentPose = this.poseGiver.getPosition();
		
		if((currentPose.getY() > Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE < 1500) || (currentPose.getY() < Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE > 1500)){
			
			Point destination;
			
			if(Main.Y_OBJECTIVE_WHITE < 1500){
				destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE - 50) );
			}
			else{
				destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE + 50) );
			}
			
			this.ia.pushGoal(this);
			this.ia.pushGoal(this.gf.goalGoToPosition(destination, OrderType.FORBIDEN));
		}
		else if(this.poseGiver.getAreaId() == 15 || this.poseGiver.getAreaId() == 0 || this.poseGiver.getAreaId() == 14){
			
			// on se place perpendiculairement au mur si ce n'est pas déjà fait
			
			Point wall ;
			if(Main.Y_OBJECTIVE_WHITE < 1500){
				wall = new Point(currentPose.getX(), 0 );
			}
			else{
				wall = new Point(currentPose.getX(), 3000 );
			}
			int angle = (int) currentPose.relativeBearing(wall);
			
			this.ia.turnHere(angle);
			
			// on lance l'ordre de lacher le palet
			
			this.ia.open();
			
			//this.ia.syncWait(Main.DROP_DELAY);
			
			// on recule de 20cm
			
			this.ia.goBackward(200);
			
			// on a finit
		}
		else{
			if(this.restart){
				this.restart = false;
				this.ia.pushGoal(this);
			}
			else{
				this.ia.open();
				this.ia.goBackward(200);
				Main.HAVE_PALET = false;
			}
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
