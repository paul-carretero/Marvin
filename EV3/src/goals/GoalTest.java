package goals;

import aiPlanner.Marvin;
import interfaces.PoseGiver;

/**
 * Objectif de déplacement à un point donné a la vitesse maximal
 */
public class GoalTest extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType 	NAME = GoalType.TEST;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	protected final PoseGiver	pg;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public GoalTest(final GoalFactory gf, final Marvin ia, final PoseGiver pg) {
		super(gf, ia);
		this.pg = pg;
	}

	@Override
	public void start() {
		this.ia.goForward(200);
		this.ia.turnHere(90);
		this.ia.goForward(10000);
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
