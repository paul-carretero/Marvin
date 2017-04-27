package goals;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import itemManager.CentralIntelligenceService;
import lejos.robotics.geometry.Point;

/**
 * Obtient un éventuel point d'interception, s'y rend et attends quelque seconde afin d'être percuter par le robot ennemi.
 * @author paul.carretero
 */
public class GoalIntercept extends Goal {
	
	/**
	 * Distance maximum ou l'on considère être suffisament près du point d'interception pour attendre
	 */
	private static final float					MAX_DISTANCE = 200;

	/**
	 * Calculateur de point d'interception possible
	 */
	private final CentralIntelligenceService	cis;
	
	/**
	 * PoseGiver, utilisé pour accéder aux informations sur la position du robot
	 */
	private final PoseGiver						pg;
	
	/**
	 * Le point d'interception éventuel (peut être null)
	 * @see Point
	 */
	private Point interceptPoint;

	/**
	 * @param gf une GoalFactory pour la création d'objectif
	 * @param ia une instance de la class Marvin utilisée pour accéder aux primitives offerte par le système
	 * @param cis une instance du calculateur de point d'interception
	 * @param pg une instance de PoseGiver pour obtenir des informations sur la position du robot
	 * @see Marvin
	 * @see GoalFactory
	 */
	protected GoalIntercept(final GoalFactory gf, final Marvin ia, final CentralIntelligenceService cis, final PoseGiver pg) {
		super(gf, ia);
		
		this.cis	= cis;
		this.pg		= pg;
	}

	@Override
	public GoalType getName() {
		return GoalType.INTERCEPT;
	}
	
	/**
	 * Ferme la pince pour éviter les risque de déformation à l'impact.
	 * Ne doit pas avoir de palet
	 */
	@Override
	protected boolean checkPreConditions() {
		if(!Main.HAVE_PALET){
			if(Main.HAND_OPEN){
				this.ia.grab();
			}
			return true;
		}
		return false;
	}

	@Override
	protected void start() {
		if(this.interceptPoint == null){
			this.interceptPoint = this.cis.getInterceptionLocation();
			
			if(this.interceptPoint != null){
				this.ia.pushGoal(this);
				this.ia.pushGoal(this.gf.goalGoToPosition(this.interceptPoint));
				this.ia.setSpeed(Main.MAX_SPEED);
			}
		}
		else if(this.pg.getPosition().distanceTo(this.interceptPoint) < MAX_DISTANCE){
			this.ia.setSpeed(Main.CRUISE_SPEED);
			this.ia.syncWait(5000);
		}
	}
	
}
