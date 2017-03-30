package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalDrop extends Goal{
	
	protected	final	String	NAME	= "GoalDrop";
	protected	PoseGiver	poseGiver	= null;
	protected	boolean		restart		= true;

	public GoalDrop(GoalFactory gf, Marvin ia, int timeout, PoseGiver p) {
		super(gf, ia, timeout);
		this.poseGiver = p;
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.HAVE_PALET);
	}

	@SuppressWarnings("unused")
	@Override
	protected void start() {
		Pose currentPose = poseGiver.getPosition();
		
		if((currentPose.getY() > Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE < 1500) || (currentPose.getY() < Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE > 1500)){
			Point destination;
			
			if(Main.Y_OBJECTIVE_WHITE < 1500){
				destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE - 50) );
			}
			else{
				destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE + 50) );
			}
			
			ia.pushGoal(this);
			ia.pushGoal(gf.goalGoToPosition(timeout, destination, OrderType.FORBIDEN));
		}
		else if(poseGiver.getAreaId() == 15 || poseGiver.getAreaId() == 0 || poseGiver.getAreaId() == 14){
			
			// on se place perpendiculairement au mur si ce n'est pas déjà fait
			
			Point wall ;
			if(Main.Y_OBJECTIVE_WHITE < 1500){
				wall = new Point(currentPose.getX(), 0 );
			}
			else{
				wall = new Point(currentPose.getX(), 3000 );
			}
			int angle = (int) currentPose.relativeBearing(wall);
			
			if(Math.abs(angle) > 25){
				ia.turnHere(angle);
			}
			
			// on lance l'ordre de lacher le pallet
			
			ia.open();
			
			syncWait(Main.DROP_DELAY);
			
			// on recule de 10cm
			
			ia.goBackward(200);
			
			// on a finit
		}
		else{
			if(restart){
				restart = false;
				ia.pushGoal(this);
			}
			else{
				ia.open();
				ia.goBackward(200);
			}
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}
