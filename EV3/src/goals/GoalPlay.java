package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;

/**
 * Encapsule la gestion de la partie, va ajouter les goal grabAndDrop par exemple.
 * @author paul.carretero, florent.chastagner
 */
public class GoalPlay extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.PLAY;
	
	/**
	 * EyeOfMarvin, gestionnaire des items de la map
	 */
	private ItemGiver eom;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param eom EyeOfMarvin gestionnaire des items sur la map
	 */
	public GoalPlay(final GoalFactory gf, final Marvin ia, final ItemGiver eom) {
		super(gf, ia);
		this.eom = eom;
	}

	@Override
	public void start() {
		if(this.eom.canPlayAgain()){
			this.ia.pushGoal(this);
			this.ia.pushGoal(this.gf.goalGrabAndDropPalet());
		}
		if(Main.I_ALSO_LIKE_TO_LIVE_DANGEROUSLY && Main.TIMER.getElapsedMin() > 3 && Main.RANDOMIZER.nextBoolean()){
			this.ia.pushGoal(this.gf.goalIntercept());
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
