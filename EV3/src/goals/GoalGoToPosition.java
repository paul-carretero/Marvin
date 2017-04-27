package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Objectif de déplacement à un point donné a la vitesse maximal.
 * @author paul.carretero
 */
public class GoalGoToPosition extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType 	NAME = GoalType.GO_TO_POSITION;
	
	/**
	 * Un point (lejos) de destination
	 */
	protected final Point 		destinationPoint;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	protected final PoseGiver	pg;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param p Un point de destination
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public GoalGoToPosition(final GoalFactory gf, final Marvin ia, final Point p, final PoseGiver pg) {
		super(gf, ia);
		this.destinationPoint = p;
		this.pg = pg;
	}

	@Override
	public void start() {
		Pose currentPose = this.pg.getPosition();

		float angle = currentPose.relativeBearing(this.destinationPoint);
		float distance = currentPose.distanceTo(this.destinationPoint);
		
		this.ia.turnHere(angle);
		
		this.ia.setSpeed(Main.MAX_SPEED);
		this.ia.goForward(distance);
		this.ia.setSpeed(Main.CRUISE_SPEED);
		
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
