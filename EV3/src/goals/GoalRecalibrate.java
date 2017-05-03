package goals;

import java.util.List;

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
	 * Distance a parcourir afin de calibrer l'angle (minimum assez fiable)
	 */
	private static final int CALIBRATE_DIST = 300;

	/**
	 * marge d'erreur pour la r�cup�ration de l'item de fin
	 */
	private static final int MARGE_ERREUR = 100;
	
	/**
	 * Minute a laquelle une tentative de recalibration rapide a �t� r�alis�e
	 */
	private static int lastFastTry = -1;

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
	
	/**
	 * recherchera une ligne pour se resynchronizer.
	 * plus lent du coup.
	 */
	private void pessimistRecalibrate(){
		
		lastFastTry = -1;
		
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
		
		this.ia.syncWait(150);
		
		if(color != Color.GREY){
			List<IntPoint> initialList = this.eom.searchPosition(color);
			
			Main.printf("[GOAL RECALIBRATE]       : initial list :  ");
			for(IntPoint p : initialList){
				Main.printf("[GOAL RECALIBRATE]       : " + p);
			}
			
			this.ia.goForward(CALIBRATE_DIST);
			
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
				List<IntPoint> resList = this.eom.searchPosition(start, CALIBRATE_DIST-MARGE_ERREUR, CALIBRATE_DIST+MARGE_ERREUR);
				
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
			this.ia.turnHere(Main.RANDOMIZER.nextInt(360) - 180);
			this.ia.pushGoal(this);
		}
	}
	
	/**
	 * Tente de mettre � jour la pose de mani�re rapide (au d�triment de la pr�cision)
	 * @return vrai si l'on a pu mettre � jour la pose de mani�re plus ou moins certaine, faux sinon
	 */
	private boolean fastTry(){
		
		lastFastTry = Main.TIMER.getElapsedMin();
		
		this.ia.goBackward(400);
		
		List<IntPoint> startList = this.eom.getNewItem();
		
		// on consid�re que l'on peut tester avec un 2 item inconnu max
		
		if(startList.size() > 2){
			return false;
		}
		
		this.ia.goBackward(CALIBRATE_DIST);
		
		List<IntPoint> endList = this.eom.getNewItem();
		
		// on consid�re que l'on peut tester avec un 2 item inconnu max
		
		if(startList.size() > 2){
			return false;
		}
		
		Pose myPose = null;
		float h = 0;
		
		for(IntPoint start : startList){
			for(IntPoint end : endList){
				if(Main.areApproximatelyEqual(start.getDistance(end), CALIBRATE_DIST, MARGE_ERREUR)){
					
					// si ambiguit�
					if(myPose != null){
						return false;
					}
					
					h = getAngle(start.toLejosPoint(), end.toLejosPoint());
					myPose = new Pose(end.x(),end.y(),h);
				}
			}
		}
		
		if(myPose == null){
			return false;
		}
		
		this.pg.setPose(myPose, true);		
		return true;
	}
	
	
	
	@Override
	public void start() {
		if(lastFastTry < Main.TIMER.getElapsedMin() && !fastTry()){
			pessimistRecalibrate();
		}
	}
	
	/**
	 * @param start point de d�part
	 * @param end point d'arriv�
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
