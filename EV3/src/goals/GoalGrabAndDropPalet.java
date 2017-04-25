package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import lejos.robotics.geometry.Point;
import shared.IntPoint;

/**
 * Objectif permettant de rechercher un palet et d'ajouter les objectif de grab et de drop sur ce palet.
 */
public class GoalGrabAndDropPalet extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType	NAME = GoalType.GRAB_AND_DROP;
	
	/**
	 * EyeOfMarvin, permet de fournir les position
	 */
	private final ItemGiver		eom;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param eom EyeOfMarvin, permet de fournir les position
	 */
	public GoalGrabAndDropPalet(final GoalFactory gf,  final Marvin ia, final ItemGiver eom) {
		super(gf, ia);
		this.eom = eom;
	}
	
	@Override
	public void start() {
		IntPoint palet = this.eom.getNearestpalet();		
		if(palet != null){
			this.ia.pushGoal(this.gf.goalDrop());
			this.ia.pushGoal(this.gf.goalGrab(palet.toLejosPoint()));
		}
		else{
			this.ia.pushGoal(this.gf.goalGoToPosition(new Point(Main.X_INITIAL, Main.Y_INITIAL)));
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}

	@Override
	protected boolean checkPreConditions() {
		if(!Main.HAND_OPEN){
			this.ia.open();
		}
		return true;
	}
}
