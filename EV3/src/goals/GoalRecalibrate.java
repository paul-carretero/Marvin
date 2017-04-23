package goals;

import java.util.List;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Color;
import shared.IntPoint;

/**
 * Objectif de recalibration de la pose du robot
 * Recalibre la position et l'angle en recherchant une ligne significative a exploiter.
 * @see Pose
 */
public class GoalRecalibrate extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType NAME = GoalType.RECALIBRATE;
	
	/**
	 * EyeOfMarvin, permet de fournir les positions des items
	 */
	private final ItemGiver	eom;
	/**
	 * 
	 */
	private final PoseGiver	pg;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param eom EyeOfMarvin, permet de fournir les positions des items
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public GoalRecalibrate(final GoalFactory gf, final Marvin ia, final ItemGiver eom, final PoseGiver pg) {
		super(gf, ia);
		this.eom	= eom;
		this.pg		= pg;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Effectue un demi-tour et ajoute un objectif de recalibreation dans la pile d'objectif pour tenter à nouveau de se recalibrer
	 */
	private void tryAgain(){
		this.ia.turnHere(180);
		this.ia.pushGoal(this.gf.goalRecalibrate());
	}

	@Override
	// si ligne blanche = demi tour
	public void start() {
		this.ia.addMeWakeUpOnColor();
		
		this.ia.setSpeed(Main.RESEARCH_SPEED);
		
		this.ia.goForward(2000);
		this.ia.goBackward(5);
		
		Color color = this.ia.getColor();
		
		if(color == Color.WHITE){
			tryAgain();
		}
		else if(color == Color.GREY || color == Color.BLACK){
			this.ia.pushGoal(this.gf.goalRecalibrate());
		}
		else{
			
			List<IntPoint> initialList = this.eom.searchPosition(color);
			
			this.ia.turnHere(180);
			
			this.ia.goForward(300);
			
			List<IntPoint> finalList = this.eom.searchPosition(color);
			
			IntPoint start	= null;
			boolean error	= false;
			
			for(IntPoint p : initialList){
				if(!finalList.contains(p)){
					if(start != null){
						error = true;
					}
					start = p;
				}
			}
			
			// si il n'y en a eu qu'un qui est supprimé de la ligne
			if(!error && start != null){
				List<IntPoint> resList = this.eom.searchPosition(start, 200, 400);
				
				if(resList.size() == 1){
					IntPoint me = resList.get(0);
					float angle = getAngle(start.toLejosPoint(), me.toLejosPoint());
					
					Pose myPose = new Pose(me.x(), me.y(), angle);
					
					this.pg.setPose(myPose);
					Main.printf("calculated pose = " + myPose);
				}
				else{
					tryAgain();
				}
			}
			else{
				tryAgain();
			}
			
		}
		
		this.ia.setSpeed(Main.CRUISE_SPEED);
		this.ia.removeMeWakeUpOnColor();
	}
	
	/**
	 * @param start point de départ
	 * @param end point d'arrivé
	 * @return l'angle (convention Lejos) entre les point start et end.
	 */
	private static float getAngle(final Point start, final Point end){
		if(start != null && end != null){
			return start.angleTo(end);
		}
		return 0;
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}
}
