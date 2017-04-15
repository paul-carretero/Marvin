package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Objectif de déplacement à un point donné
 */
public class GoalGoToPosition extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.GO_TO_POSITION;
	
	/**
	 * Un point (lejos) de destination
	 */
	protected Point 		destinationPoint;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	protected PoseGiver		pg;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param p Un point de destination
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public GoalGoToPosition(GoalFactory gf, Marvin ia, Point p, PoseGiver pg) {
		super(gf, ia);
		this.destinationPoint = p;
		this.pg = pg;
	}

	@Override
	public void start() {
		Pose currentPose = this.pg.getPosition();

		int angle = (int) currentPose.relativeBearing(this.destinationPoint);
		int distance = (int) currentPose.distanceTo(this.destinationPoint);
		
		this.ia.turnHere(angle);
		this.ia.goForward(distance);
		
		Main.HAS_MOVED = true;
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
