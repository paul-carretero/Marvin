package positionManager;

import java.util.ArrayList;
import java.util.List;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.PoseListener;
import interfaces.SignalListener;
import shared.IntPoint;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;
/**
 * Classe principale centralisant les informations de position. <br>
 * re�oit les informations de d�placement et calcule la coh�rence en fonction des capteurs.<br>
 * Est normalement invoqu� par l'IA a chaque d�placement afin de sp�cifier quand rechercher sa position.
 * @author paul.carretero, florent.chastagner
 */
public class PositionCalculator implements PoseGiver {

	/**
	 * Pourcentage de la position de la carte qui sera utilis�e pour calculer la position final une fois le mouvement arr�t�
	 */
	private static final float		MAP_PERCENT			= 0.4f;
	
	/**
	 * Distance maximum entre le point donn� et le point calcul� (en mm) avant qu'un gestionnaire de position se d�clare en �tat de "lost".
	 */
	private static final int		MAX_SAMPLE_ERROR	= 270;
	
	/**
	 * Instance du radar permettant de retourne la distance vers un objet situ� devant le robot
	 */
	private final DistanceGiver 	radar;
	
	/**
	 * Pose provider fourni par la librairie LeJos, utilis� comme base pour suivre les d�placements du robot
	 */
	private final OdometryPoseProvider odometryPoseProvider;
	
	/**
	 * Classe centralisant les donn�es de type IA, notament les interruptions ou les demandes de navigation
	 */
	private final SignalListener	marvin;
	
	/**
	 * Interface retournant la position des item sur la carte, dont le robot
	 */
	private final ItemGiver			eom;
	
	/**
	 * Gestionnaire d'area permettant de r�cup�rer des informations sur les couleurs 
	 * et de mettre � jour sa pose en fonction de la position des lignes travers�es
	 */
	private final AreaGiver 		area;
	
	/**
	 * Vrai si le positionManager se consid�re comme perdu, faux sinon
	 */
	private volatile Point			estimatedDest;

	/**
	 * Liste de pose listener � mettre � jour � chaque fin de parcours
	 */
	private final List<PoseListener> poseListeners;
	
	/**
	 * Initialise les principaux param�tre initiaux du gestionnaire de position.
	 * @param mp le pilot du robot (de la librairie LeJos)
	 * @param radar Une instance du radar du robot
	 * @param ia le gestionnaire de l'IA et des objectifs
	 * @param areaManager un gestionnaire de couleur et area
	 * @param eom eyeOfMarvin, gestionnaire de position des items
	 */
	public PositionCalculator(final MoveProvider mp, final DistanceGiver radar, final SignalListener ia, ItemGiver eom, AreaGiver areaManager){
		this.radar 					= radar;
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.marvin					= ia;
		this.poseListeners			= new ArrayList<PoseListener>();
		this.eom					= eom;
		this.area					= areaManager;
		
		initPose();
		
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	/********************************************************
	 * Correction de la position du robot
	 *******************************************************/
	
	/**
	 * Tente de mettre � jour la pose (position seulement) du robot en fonction des informations re�ues
	 * Met eventuellement � jour l'area
	 * @return La distance avec la position sur la map
	 */
	private float updatePose() {
		
		Pose myPose = getPosition();
		this.area.updatePose(myPose);
		this.odometryPoseProvider.setPose(myPose);
		broadcastPose();
		
		float dist = mapPositionUpdate();
		broadcastPose();
		
		if(checkRadarConsistancy() || !Main.USE_RADAR){
			this.area.updateArea(false);
		}

		return dist;
	}
	
	/**
	 * Mets � jour la position en fonction des donn�es de l'item le plus proche sur la map.
	 * @return la distance entre la position theroque et celle sur la carte
	 */
	synchronized private float mapPositionUpdate() {
		Pose myPose = this.odometryPoseProvider.getPose();
		IntPoint me = this.eom.getMarvinPosition();
		float distance = Integer.MAX_VALUE;
		if(me != null){
			
			distance = myPose.distanceTo(me.toLejosPoint());
			
			Main.poseRealToSensor(myPose);
			
			float x = me.x() * (MAP_PERCENT) + myPose.getX() * (1 - MAP_PERCENT);
			float y = me.y() * (MAP_PERCENT) + myPose.getY() * (1 - MAP_PERCENT);
			
			myPose.setLocation(x, y);
			
			Main.poseSensorToReal(myPose);
			
			this.odometryPoseProvider.setPose(myPose);
		}
		return distance;
	}
	
	/********************************************************
	 * Modification de la position du robot
	 *******************************************************/

	synchronized public Pose getPosition() {
		return this.odometryPoseProvider.getPose();
	}

	synchronized public void setPose(Pose p, boolean updateArea) {
		this.odometryPoseProvider.setPose(p);
		broadcastPose();
		mapPositionUpdate();
		broadcastPose();
		this.area.updateArea(updateArea);
	}
	
	/**
	 * D�finie la pose actuelle du robot comme la pose initiale au commencement du jeu
	 */
	public final void initPose(){
		this.odometryPoseProvider.setPose(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
		broadcastPose();
	}
	
	/**
	 * @param pl un PoseListener a ajouter
	 */
	synchronized public void addPoseListener(final PoseListener pl){
		this.poseListeners.add(pl);
	}
	
	/**
	 * Envoit a tous les PoseListener la derni�re position de la pose
	 */
	synchronized private void broadcastPose(){
		Pose mypose = this.odometryPoseProvider.getPose();
		for(PoseListener p : this.poseListeners){
			p.setPose(mypose);
		}
	}
	
	/********************************************************
	 * Notifications des types de mouvements
	 *******************************************************/
	
	/**
	 * on ne met � jour le position que si l'on a effectu� un d�placement de type TRAVEL (lin�aire)
	 * si on est en marche arriere alors l'ia nous indiquera notre position
	 * @param distance distance parcouru sur la ligne
	 */
	synchronized public void startLine(final float distance) {
		Pose myCurrentPose = this.odometryPoseProvider.getPose();
		myCurrentPose.moveUpdate(distance);
		this.estimatedDest = myCurrentPose.getLocation();
		
		Main.log("[POSITION CALCULATOR]   : depart sur : " + this.odometryPoseProvider.getPose().toString());
		Main.log("[POSITION CALCULATOR]   : a l'arret, position future estimee : " + this.estimatedDest);
	}

	/**
	 * Operation a effectuer pour garantire la consistance de la position a la fin d'un deplacement en type lieaire
	 * @param everythingFine vrai si le deplacement n'a pas ete interrompu, faux sinon
	 */
	synchronized public void endLine(final boolean everythingFine){
		Main.log("[POSITION CALCULATOR]   : arrivee sur la position estimee : " + this.odometryPoseProvider.getPose().toString());
		
		broadcastPose();
		float dist = updatePose();
		broadcastPose();
		
		Main.printf("[POSITION CALCULATOR]   : (linear) Position fixee : " + this.odometryPoseProvider.getPose().toString());
			
		if(everythingFine){
			
			float errorDistance = getPosition().distanceTo(this.estimatedDest);
			if((errorDistance > MAX_SAMPLE_ERROR && !Main.PRESSION) || dist > MAX_SAMPLE_ERROR * 1.5){
				this.marvin.signalLost();
			}
			broadcastPose();
		}
		this.estimatedDest = null;
	}
	
	/**
	 * Operation a effectuer pour garantire la consistance de la position a la fin d'un deplacement en type turnHere
	 */
	synchronized public void endTurn(){
		Main.printf("[POSITION CALCULATOR]   : (turn) arrivee sur la position estimee : " + this.odometryPoseProvider.getPose().toString());	
		broadcastPose();
		this.estimatedDest = null;
	}
	
	/********************************************************
	 * Calcul de coherence des informations de positions
	 *******************************************************/
	
	/**
	 * Utilis� pour d�tecter une perte en fonction des donn�es radar, le radar n'est toutefois pas vraiment fiable...
	 * @return vrai si la position actuelle est coh�rente avec les donn�es radar (en fonction de l'item que l'on a devant), faux sinon
	 */
	private boolean checkRadarConsistancy() {
		if(!Main.USE_RADAR){
			return true;
		}
		int radarDistance = this.radar.getRadarDistance();
		if(radarDistance < Main.RADAR_MAX_RANGE && radarDistance > Main.RADAR_MIN_RANGE){
		
			Pose tempPose = this.odometryPoseProvider.getPose();
			tempPose.moveUpdate(radarDistance);
			
			// si on a pas detecter un mur... avec 3 cm de marge d'erreur
			if(tempPose.getX() > 30 && tempPose.getX() < 1970 && tempPose.getY() < 2970 && tempPose.getY() > 30){
				IntPoint nearest = this.eom.getNearestItem(new IntPoint(tempPose.getLocation()));
				
				if(nearest != null){
					Point bestMatch = nearest.toLejosPoint();
					
					tempPose = this.odometryPoseProvider.getPose();
					
					return Math.abs(tempPose.distanceTo(bestMatch) - radarDistance) < MAX_SAMPLE_ERROR;
				}
			}
		}
		return true;
	}
}
