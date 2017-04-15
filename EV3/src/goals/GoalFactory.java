package goals;

import java.util.ArrayDeque;
import java.util.Deque;

import aiPlanner.Marvin;
import goals.Goal.OrderType;
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
	 * instance de Marvin, gestionnaire de l'ia et des moteurs
	 */
	private	Marvin						ia;
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	private	PoseGiver					pg;
	/**
	 * EyeOfMarvin, permet de fournir les position
	 */
	private	ItemGiver					eom;
	/**
	 * Permet de stocker l'état de la dernière tentative degrab pour décider si on doit choisir un objectif de grab de type optimiste ou pessimiste à l'avenir
	 */
	private	boolean 					lastGrabOk;
	/**
	 * un DistanceGiver permettant de donner des distance radar
	 */
	private	DistanceGiver				radar;
	/**
	 * CentralIntelligenceService permettant de donner des points d'interceptions
	 */
	private CentralIntelligenceService	cis;
	
	/**
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param eom EyeOfMarvin, permet de fournir les position
	 * @param radar un DistanceGiver permettant de donner des distance radar
	 * @param cis CentralIntelligenceService permettant de donner des points d'interceptions
	 */
	public GoalFactory(Marvin ia, PoseGiver pg, ItemGiver eom, DistanceGiver radar, CentralIntelligenceService	cis){
		this.ia			= ia;
		this.pg			= pg;
		this.eom		= eom;
		this.lastGrabOk = true;
		this.radar		= radar;
		this.cis		= cis;
	}
	
	/**
	 * @return une pile d'objectif initialisé avec les objectif de départ
	 */
	public Deque<Goal> initializeStartGoals(){
		Deque<Goal> goals = new ArrayDeque<Goal>();
		//goals.push(play());
		goals.push(goalRecalibrate());
		//goals.push(goalGrabAndDropPalet());
		return goals;
	}
	
	/**
	 * Met à jour le status du dernier grab.
	 * @param b vrai si le dernier grab a réussi, faux sinon
	 */
	public void setLastGrab(boolean b){
		this.lastGrabOk = b;
	}
	
	/**
	 * @return un objectif de type play
	 */
	public Goal play(){
		return new GoalPlay(this, this.ia);
	}
	
	/**
	 * @param destination un point de destination
	 * @param backward Ordre sur la nécéssité de se déplacer en marche arrière
	 * @return un objectif de type GoToPosition
	 */
	public Goal goalGoToPosition(Point destination, OrderType backward){
		return new GoalGoToPosition(this,this.ia,destination,backward,this.pg);
	}
	
	/**
	 * @return un objectif de Drop
	 */
	public Goal goalDrop(){
		return new GoalDrop(this,this.ia,this.pg);
	}
	
	/**
	 * @param palet une position d'un palet
	 * @return un objectif de Grab sur un palet
	 */
	public Goal goalGrab(Point palet){
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
		return new GoalRecalibrate(this, this.ia, this.eom, this.pg);
	}
	/**
	 * @return Un objectif d'interception
	 */
	public Goal goalIntercept(){
		return new GoalIntercept(this,this.ia,this.cis,this.pg);
	}
}
