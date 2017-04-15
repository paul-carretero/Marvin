package goals;


import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

/**
 * Permet d'effectuer un Grab de mani�re pessimist (on recherche avant la position consid�r� comme la meilleure).
 */
public class GoalGrabPessimist extends Goal {
	
	/**
	 * Nom de l'objectif
	 */
	protected final GoalType		NAME = GoalType.GRAB_PESSIMISTE;
	
	/**
	 * palet une position d'un palet possible
	 */
	protected 		Point			palet;
	
	/**
	 * pg PoseGiver permettant de retourner une pose du robot
	 */
	protected 		PoseGiver		pg;
	
	/**
	 * eom EyeOfMarvin, permet de fournir les position
	 */
	protected		ItemGiver		eom;
	
	/**
	 * un DistanceGiver permettant de donner des distance radar
	 */
	protected		DistanceGiver 	radar;
	
	/**
	 * Marge autoris� par rapport � la distance radar consid�r� fiable
	 */
	protected final static int		MARGE = 100;

	/**
	 * @param gf le GoalFactory
	 * @param ia instance de Marvin, gestionnaire de l'ia et des moteurs
	 * @param palet une position d'un palet possible
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 * @param eom EyeOfMarvin, permet de fournir les position
	 * @param radar un DistanceGiver permettant de donner des distance radar
	 * @see Pose
	 */
	public GoalGrabPessimist(GoalFactory gf, Marvin ia, Point palet, PoseGiver pg, ItemGiver eom, DistanceGiver radar) {
		super(gf, ia);
		this.radar	= radar;
		this.eom 	= eom;
		this.palet	= palet;
		this.pg 	= pg;
	}

	/**
	 * Analyse la distance s�parant le robot du palet.
	 * Si le robot n'est pas dans la port� radar consid�r� comme fiable alors
	 * fait avancer ou reculer le robot pour le mettre � port� radar fiable
	 */
	protected void correctPosition(){
		Pose currentPose = this.pg.getPosition();

		int distance = (int)currentPose.distanceTo(this.palet);
		int angleCorrection = (int)currentPose.relativeBearing(this.palet);
		
		this.ia.turnHere(angleCorrection);
		
		if(distance < (Main.RADAR_DEFAULT_RANGE - MARGE)){
			this.ia.goBackward(Main.RADAR_DEFAULT_RANGE - distance);
		}
		else if(distance > Main.RADAR_DEFAULT_RANGE + MARGE){
			this.ia.goForward(distance - Main.RADAR_DEFAULT_RANGE);
		}
	}
	
	/**
	 * Tente de grab un palet si on a la pression du sensor.
	 * @return true si on a pu grab un palet, faux sinon.
	 */
	protected boolean tryGrab(){
		if(Main.PRESSION){
			Main.HAVE_PALET = true;
			this.ia.grab();
			return true;
		}
		return false;
	}
	
	/**
	 * Met � jour a vrai le lastGrabStatus de goalFactory en fonction du status de la derni�re tentative de grab.
	 */
	protected void updateStatus(){
		this.gf.setLastGrab(Main.HAVE_PALET);
	}
	
	/**
	 * Encapsule toutes les fonctionnalit�s li�es au grab.
	 * Ne grab que si un palet est d�tect� par le sensor.
	 * V�rifie au radar qu'un palet existe avant de tenter.
	 */
	protected void grabWrapper(){
		Pose currentPose 	= this.pg.getPosition();
		int distance 		= (int)currentPose.distanceTo(this.palet);
		
		this.ia.setAllowInterrupt(true);
		
		if(this.radar.checkSomething()){
			
			this.ia.goForward(distance);
			
			if(!tryGrab()){			
				failGrabHandler();
			}
		}
				
		this.ia.setAllowInterrupt(false);
	}
	
	/**
	 * Recherche le meilleur angle d'approche en fonction du retour de distance du radar.
	 * Recherche le meilleur angle dans a + ou - 25� en fonction de l'angle initial.
	 */
	private void setBestAngle(){
		int radarDistance 	= Main.RADAR_OUT_OF_BOUND;
		int previousRadar 	= Main.RADAR_OUT_OF_BOUND;
		
		this.ia.turnHere(-25);
		radarDistance = this.radar.getRadarDistance();
		previousRadar = radarDistance;
		this.ia.turnHere(25);
		radarDistance = this.radar.getRadarDistance();
		if (radarDistance > previousRadar){
			this.ia.turnHere(-25);
		}
		else{
			previousRadar = radarDistance;
			this.ia.turnHere(25);
			radarDistance = this.radar.getRadarDistance();
			if (radarDistance > previousRadar){
				this.ia.turnHere(-25);
			}
		}
	}
	
	@Override
	public void start() {
		
		if(this.eom.checkpalet(new IntPoint(this.palet))){
			
			correctPosition();
			setBestAngle();
			grabWrapper();
			
		}
		
		updateStatus();
	}

	/**
	 * Si on a pas r�ussi a grab directement alors on tente de reculer, tourner l�g�rement et retenter.
	 * Un grab n'est pas r�ussi si on a pas eu de confirmation du capteur de pression.
	 */
	protected void failGrabHandler() {
		this.ia.goBackward(200);
		this.ia.turnHere(13);
		this.ia.goForward(225);
		
		if(!tryGrab()){
			this.ia.goBackward(225);
			this.ia.turnHere(-26);
			this.ia.goForward(250);
			tryGrab();
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}

	@Override
	protected boolean checkPreConditions() {
		return Main.HAND_OPEN && !Main.HAVE_PALET;
	}
}
