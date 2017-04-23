package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Objectif de drop d'un palet (ne s'éxécutera que si l'on dispose d'un palet)
 */
public class GoalDrop extends Goal{
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.DROP;
	
	/**
	 * Nombre maximum de tentative avant d'abandonner le palet
	 */
	private static final int MAX_TRY = 3;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	protected final PoseGiver poseGiver;
	
	/**
	 * autorise ou non le rédémarrage de l'objectif si il n'a pas pu être terminé
	 */
	protected	int			tryCount;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public GoalDrop(final GoalFactory gf, final Marvin ia, final PoseGiver pg) {
		super(gf, ia);
		this.poseGiver	= pg;
		this.tryCount	= 0;
	}

	@Override
	protected boolean checkPreConditions() {
		return Main.HAVE_PALET;
	}
	
	/**
	 * Procedure pour lacher un palet et s'en écarter en marche arrière de 20 cm
	 */
	private void drop(){
		this.ia.open();
		Main.HAVE_PALET = false;
		this.ia.goBackward(200);
	}
	
	/**
	 * Tente de faire déplacer le robot jusqu'a la zone de drop des palet (but adverse).
	 * @param currentPose la pose actuelle du robot
	 */
	private void goToDropZone(final Pose currentPose){
		Point destination;
		
		if(Main.Y_OBJECTIVE_WHITE < 1500){
			destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE - 50) );
		}
		else{
			destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE + 50) );
		}
		
		this.ia.pushGoal(this);
		this.ia.pushGoal(this.gf.goalGoToPosition(destination));
		
		this.tryCount++;
	}


	@Override
	protected void start() {
		
		Pose currentPose = this.poseGiver.getPosition();
		
		if((currentPose.getY() < Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE < 1500) || (currentPose.getY() > Main.Y_OBJECTIVE_WHITE && Main.Y_OBJECTIVE_WHITE > 1500)){
			drop();
		}
		else{
			if(this.tryCount == 0){
				goToDropZone(currentPose);
			}
			else if(this.tryCount < MAX_TRY){
				this.ia.turnHere(90);
				this.ia.goForward(200);
				this.ia.turnHere(-90);
				goToDropZone(currentPose);
			}
			else{
				drop();
			}
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
