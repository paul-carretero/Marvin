package goals;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import aiPlanner.Marvin;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import itemManager.CentralIntelligenceService;
import lejos.robotics.geometry.Point;

/**
 * Factory permettant de construire une pile d'objectif et de construire des objectif à l'aide de primitive simple
 */
public class GoalFactory {
	
	/**
	 * Nombre maximum de grab en echec avant d'ajouter un objectif de recalibration
	 */
	private static final int MAX_FAILED_GRAB = 6;
	/**
	 * instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	private	final Marvin		ia;
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	private	final PoseGiver		pg;
	/**
	 * EyeOfMarvin, permet de fournir les position
	 */
	private	final ItemGiver		eom;
	/**
	 * Permet de stocker l'état de la dernière tentative degrab pour décider si on doit choisir un objectif de grab de type optimiste ou pessimiste à l'avenir
	 */
	private	boolean 			lastGrabOk;
	/**
	 * Permet de stocker l'état de la dernière tentative degrab pour décider si on doit choisir un objectif de grab de type optimiste ou pessimiste à l'avenir
	 */
	private	int		 			failGrabCount = 0;
	/**
	 * un DistanceGiver permettant de donner des distance radar
	 */
	private	final DistanceGiver	radar;
	/**
	 * CentralIntelligenceService permettant de donner des points d'interceptions
	 */
	private final CentralIntelligenceService cis;
	
	/**
	 * Gestionnaire des area et des couleurs
	 */
	private final AreaGiver areaManager;
	
	/**
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param eom EyeOfMarvin, permet de fournir les position
	 * @param radar un DistanceGiver permettant de donner des distance radar
	 * @param cis CentralIntelligenceService permettant de donner des points d'interceptions
	 * @param areaManager gestionnaire de couleurs
	 */
	public GoalFactory(final Marvin ia, final PoseGiver pg, final ItemGiver eom, final DistanceGiver radar, final CentralIntelligenceService cis, final AreaGiver areaManager){
		this.ia			= ia;
		this.pg			= pg;
		this.eom		= eom;
		this.lastGrabOk = true;
		this.radar		= radar;
		this.cis		= cis;
		this.areaManager = areaManager;
	}
	
	/**
	 * @return une pile d'objectif initialisé avec les objectif de départ
	 */
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ConcurrentLinkedDeque<Goal>();
		goals.push(play());
		//goals.push(goalTest());
		return goals;
	}
	
	/**
	 * Met à jour le status du dernier grab.
	 * @param b vrai si le dernier grab a réussi, faux sinon
	 */
	public void setLastGrab(final boolean b){
		this.lastGrabOk = b;
		if(this.lastGrabOk){
			this.failGrabCount = 0;
		}
		else{
			this.failGrabCount++;
			if(this.failGrabCount > MAX_FAILED_GRAB){
				this.ia.pushGoal(goalRecalibrate());
				this.failGrabCount = 0;
			}
		}
	}
	
	/**
	 * @return un objectif de type play
	 */
	public Goal play(){
		return new GoalPlay(this, this.ia, this.eom);
	}
	
	/**
	 * @param destination un point de destination
	 * @return un objectif de type GoToPosition
	 */
	public Goal goalGoToPosition(final Point destination){
		return new GoalGoToPosition(this,this.ia,destination,this.pg);
	}
	
	/**
	 * @return un objectif de Drop
	 */
	public Goal goalDrop(){
		return new GoalDrop(this,this.ia,this.pg, this.areaManager);
	}
	
	/**
	 * @param palet une position d'un palet
	 * @return un objectif de Grab sur un palet
	 */
	public Goal goalGrab(final Point palet){
		if(this.lastGrabOk){
			return new GoalGrabOptimist(this, this.ia, palet, this.pg, this.eom, this.radar);
		}
		return new GoalGrabPessimist(this, this.ia, palet, this.pg, this.eom, this.radar);
	}
	
	/**
	 * @return un objectif de GrabAndDrop
	 */
	public Goal goalGrabAndDropPalet(){
		return new GoalGrabAndDropPalet(this, this.ia, this.eom);
	}
	
	/**
	 * @return Un Objectif de Recalibration
	 */
	public Goal goalRecalibrate(){
		return new GoalRecalibrate(this, this.ia, this.eom, this.pg, this.areaManager);
	}
	
	/**
	 * @return Un objectif d'interception
	 */
	public Goal goalIntercept(){
		return new GoalIntercept(this,this.ia,this.cis,this.pg);
	}
	
	/**
	 * @return un Objectif de test
	 */
	public Goal goalTest(){
		return new GoalTest(this, this.ia, this.pg);
	}
}
