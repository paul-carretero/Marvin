package itemManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.PoseListener;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;
import shared.Item;

/**
 * Thread analysant les position des items de type undefined. Si un seul Item de ce type existe alors il s'agit probablement du robot ennemi.
 * En capturant deux positions de robot ennemi alors on peut estimer une possibilité de point d'interception.
 * @author paul.carretero, florent.chastagner
 */
public class CentralIntelligenceService extends Thread implements PoseListener{
	
	/**
	 * EyeOfMarvin, permet de fournir les positions des items
	 */
	private final ItemGiver		eom;
	
	/**
	 * PoseGiver permettant de retourner une pose du robot
	 */
	private volatile Pose 		myPose;
	
	/**
	 * Point où il est éventuellement possible d'intercepter un robot ennemi (avant qu'il n'atteigne notre camps)
	 */
	private IntPoint			interceptionTarget;
	
	/**
	 * Distance minimum qu'un robot ennemi doit avoir parcouru (en millimètre) entre deux vérification pour être considété comme "en mouvement"
	 */
	private static final int	SIGNIFICATIVE_DISTANCE	= 70;
	
	/**
	 * Durée entre deux vérification de la carte
	 */
	private static final int	REFRESH_RATE			= 800;
	
	/**
	 * @param eom EyeOfMarvin, permet de fournir les positions des items
	 * @param pg PoseGiver permettant de retourner une pose du robot
	 */
	public CentralIntelligenceService(final ItemGiver eom, final PoseGiver pg){
		super("CentralIntelligenceService");
		this.interceptionTarget		= null;
		this.eom					= eom;
		this.myPose 				= new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		Main.printf("[CIS]                   : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[CIS]                   : started");
		this.setPriority(MIN_PRIORITY);
		
		Item firstContact			= null;
		Item confirmationContact	= null;

		while(!isInterrupted()){
			
			// si on a pas de premier contact alors on tente d'en repérer un.
			if(firstContact == null){
				updateInterceptionTarget(null);
				firstContact = this.eom.getPossibleEnnemy();
			}
			else{
				confirmationContact = this.eom.getPossibleEnnemy();
				
				// si le contact n'a pas bougé ou si il a disparu, on reset
				if(confirmationContact != firstContact && confirmationContact != null){
					
					// on vérifie que la distance est significative pour pouvoir extrapoler à une droite
					if(firstContact.getDistance(confirmationContact) > SIGNIFICATIVE_DISTANCE){
						IntPoint intersection = confirmationContact.getIntersection(firstContact.computeVector(confirmationContact));
						
						// on vérifie que l'ennemy avance vers l'intersection et que l'intersection est bien dans le terrain
						if(intersection != null && intersection.x() > 0 && intersection.x() < 3000 && confirmationContact.getDistance(intersection) < firstContact.getDistance(intersection)){
							updateInterceptionTarget(intersection);
						}
						else{
							updateInterceptionTarget(null);
						}
					}
					else{
						updateInterceptionTarget(null);
					}
				}
				else{
					updateInterceptionTarget(null);
				}
				
				// on réinitialise firstcontact à confirmationcontact (si il change de trajectoire ou autre)
				firstContact = confirmationContact;
				confirmationContact = null;
			}
			syncWait();
		}
		Main.printf("[CIS]                   : Finished");
	}
	
	/**
	 * @param target une nouveaux point d'interception calculé
	 */
	synchronized private void updateInterceptionTarget(final IntPoint target){
		this.interceptionTarget = target;
	}
	
	/**
	 * Attends pendant une durée définie (le Thread reste interruptible)
	 */
	synchronized private void syncWait(){
		try {
			this.wait(REFRESH_RATE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * @return un Point (Lejos) où il est possible qu'un ennemi se rende (passe par) prochainement (basé sur ses derniers mouvements). Retourne null si l'ennemi est plus proche de ce point que nous.
	 */
	synchronized public Point getInterceptionLocation(){
		Point ennemy = this.eom.getPossibleEnnemy().toLejosPoint();
		if(this.interceptionTarget != null 
				&& ennemy != null
				&& (this.myPose.distanceTo(this.interceptionTarget.toLejosPoint()) < ennemy.distance(this.interceptionTarget.toLejosPoint()))
		){	
			return this.interceptionTarget.toLejosPoint();
		}
		return null;
	}

	public void setPose(Pose p) {
		this.myPose = p;
	}
}
