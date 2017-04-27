package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.AreaGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Objectif de drop d'un palet (ne s'éxécutera que si l'on dispose d'un palet).
 * @author paul.carretero
 */
public class GoalDrop extends Goal{
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.DROP;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	protected final PoseGiver poseGiver;
	
	/**
	 * autorise ou non le rédémarrage de l'objectif si il n'a pas pu être terminé
	 */
	protected int			tryCount;

	/**
	 * Gestionnaire des couleurs
	 */
	private AreaGiver		am;


	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param am Gestionnaire de couleur
	 */
	public GoalDrop(final GoalFactory gf, final Marvin ia, final PoseGiver pg, AreaGiver am) {
		super(gf, ia);
		this.poseGiver	= pg;
		this.tryCount	= 0;
		this.am			= am;
	}

	@Override
	protected boolean checkPreConditions() {
		return Main.HAVE_PALET;
	}
	
	/**
	 * Procedure pour lacher un palet et s'en écarter en marche arrière de 20 cm
	 */
	private void drop(){
		Main.log("[MARVIN]                : drop palet");
		this.ia.open();
		Main.HAVE_PALET = false;
		this.ia.goBackward(350);
	}
	
	/**
	 * Tente de faire déplacer le robot jusqu'a la zone de drop des palet (but adverse).
	 * Divise en deux si la distance a parcourir est trop importante pour garantir la fiabilité
	 * @param currentPose la pose actuelle du robot
	 */
	private void goToDropZone(final Pose currentPose){
		Main.log("[GOAL]                  : go to DropZone");
		Point destination;
		
		if(Main.Y_OBJECTIVE_WHITE < 1500){
			destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE - 50) );
		}
		else{
			destination = new Point(currentPose.getX(), (Main.Y_OBJECTIVE_WHITE + 50) );
		}
		
		this.ia.pushGoal(this);
		this.ia.pushGoal(this.gf.goalGoToPosition(destination));
		
		if(Math.abs(destination.getY() - currentPose.getY()) > Main.MAX_SAFE_DISTANCE){
			float intermediateY = (float) ( (destination.getY() + currentPose.getY()) / 2f);
			
			this.ia.pushGoal(this.gf.goalGoToPosition(new Point(currentPose.getX(), intermediateY )));
		}
		
		
		this.tryCount++;
	}

	@Override
	protected void start() {
		Pose currentPose = this.poseGiver.getPosition();
		if(this.tryCount > 0){
			if(this.am.getLastLine() == Color.WHITE){
				drop();
			}
			else{
				Main.log("[GOAL]                  : Ligne Blanche non detectee");
				this.ia.goForward(100);
				drop();
			}
		}
		else{
			goToDropZone(currentPose);
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
