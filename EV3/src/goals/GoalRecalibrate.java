package goals;

import java.util.List;
import java.util.Random;

import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.AreaGiver;
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
 * @author paul.carretero, florent.chastagner
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
	 * Fourni des primitives pour la gestion des couleurs et des areas
	 */
	private final AreaGiver am;
	
	/**
	 * vrai si l'on doit recherche une couleur en marche arriere
	 * vrai une fois sur deux
	 */
	private static boolean BACKWARD = true;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param eom EyeOfMarvin, permet de fournir les positions des items
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param am Fourni des primitives pour la gestion des couleurs et des areas
	 */
	public GoalRecalibrate(final GoalFactory gf, final Marvin ia, final ItemGiver eom, final PoseGiver pg, final AreaGiver am) {
		super(gf, ia);
		this.eom	= eom;
		this.pg		= pg;
		this.am		= am;
	}
	
	@Override
	public void start() {
		
		boolean success = false;
		
		this.ia.addMeWakeUpOnColor();
		
		this.ia.setSpeed(Main.RESEARCH_SPEED);
		
		if(BACKWARD){
			this.ia.goBackward(1500);
			this.ia.goForward(5);
		}
		else{
			this.ia.turnHere(90);
			this.ia.goForward(1500);
			this.ia.goBackward(5);
		}
		BACKWARD = !BACKWARD;

		Color color = this.am.getColor();
		
		this.ia.removeMeWakeUpOnColor();
		
		Main.printf("[GOAL RECALIBRATE]       : on color : " + color);
		
		this.ia.syncWait(200);
		
		if(color != Color.GREY){
			List<IntPoint> initialList = this.eom.searchPosition(color);
			
			Main.printf("[GOAL RECALIBRATE]       : initial list :  ");
			for(IntPoint p : initialList){
				Main.printf("[GOAL RECALIBRATE]       : " + p);
			}
			
			this.ia.goForward(300);
			
			List<IntPoint> finalList = this.eom.searchPosition(color);
			
			Main.printf("[GOAL RECALIBRATE]       : final list :  ");
			for(IntPoint p : initialList){
				Main.printf("[GOAL RECALIBRATE]       : " + p);
			}
			
			IntPoint start	= null;
			
			for(IntPoint p : initialList){
				if(!finalList.contains(p)){
					start = p;
				}
			}
			
			Main.printf("[GOAL RECALIBRATE]       : start = " + start);
			
			if(start != null){
				List<IntPoint> resList = this.eom.searchPosition(start, 200, 400);
				
				Main.printf("[GOAL RECALIBRATE]       : Result list :  ");
				for(IntPoint p : resList){
					Main.printf("[GOAL RECALIBRATE]       : " + p);
				}
				
				if(resList.size() == 1){
					IntPoint me = resList.get(0);
					float angle = getAngle(start.toLejosPoint(), me.toLejosPoint());
					
					Pose myPose = new Pose(me.x(), me.y(), angle);
					
					this.pg.setPose(myPose, true);
					Main.printf("[GOAL RECALIBRATE]       : calculated pose = " + myPose);
					success = true;
				}
			}
		}
		this.ia.setSpeed(Main.CRUISE_SPEED);
		if(success){
			this.ia.signalNoLost();
		}
		else{
			Random r = new Random();
			this.ia.turnHere(r.nextInt(360) - 180);
			this.ia.pushGoal(this);
		}
		
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
