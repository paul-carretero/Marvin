package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import lejos.robotics.geometry.Point;

/**
 * Encapsule la gestion de la partie, va ajouter les goal grabAndDrop par exemple.
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
		this.ia.pushGoal(this);
		if(this.eom.canPlayAgain()){
			this.ia.pushGoal(this.gf.goalGrabAndDropPalet());
		}
		else{
			this.ia.pushGoal(this.gf.goalGoToPosition(new Point(Main.X_INITIAL, Main.Y_INITIAL)));
		}
		
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
