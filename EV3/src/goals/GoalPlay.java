package goals;

import aiPlanner.Marvin;

/**
 * Encapsule la gestion de la partie, va ajouter les goal grabAndDrop par exemple.
 */
public class GoalPlay extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.PLAY;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	public GoalPlay(final GoalFactory gf, final Marvin ia) {
		super(gf, ia);
	}

	@Override
	public void start() {
		this.ia.pushGoal(this);
		this.ia.pushGoal(this.gf.goalGrabAndDropPalet());
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
