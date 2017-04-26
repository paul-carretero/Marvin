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
 * Class principale centralisant les informations de position. re�oit ou les informations de d�placement et calcule la coh�rence en fonction des capteurs
 */
public class PositionCalculator implements PoseGiver {

	/**
	 * Pourcentage de la position de la carte qui sera utilis� pour calculer la position final une fois le mouvement arr�t�
	 */
	private static final float		MAP_PERCENT			= 0.5f;
	
	/**
	 *  Pourcentage de la position du areaManager qui sera utilis� pour calculer la position final une fois le mouvement arr�t�
	 */
	private static final float		AREA_PERCENT		= 0f;
	
	/**
	 * Distance au dela de laquelle on consid�re l'area comme invalidee
	 */
	private static final float		AREA_LOST			= 300f;
	
	/**
	 * Distance maximum entre le point donn� et le point calcul� (en mm) avant qu'un gestionnaire de position se d�clare en �tat de "lost".
	 */
	private static final int		MAX_SAMPLE_ERROR	= 300;
	
	/**
	 * Instance du radar permettant de retourne la distance vers un objet situ� devant le robot
	 */
	private final DistanceGiver 	radar;
	
	/**
	 * Pose provider fourni par la librairie LeJos, utilis� comme base pour suivre les d�placement du robot
	 */
	private final OdometryPoseProvider odometryPoseProvider;
	
	/**
	 * Classe centralisant les donn�es de type IA, notament les interruption ou les demandes de navigation
	 */
	private final SignalListener	marvin;
	
	/**
	 * Interface retournant la position des item sur la carte, dont le robot
	 */
	private final ItemGiver			eom;
	
	/**
	 * Classe donnant l'area actuelle sur laquelle est le robot.
	 */
	private final AreaGiver 		area;
	
	/**
	 * Vrai si le positionManager se consid�re comme perdu, faux sinon
	 */
	private volatile Point			estimatedDest;

	/**
	 * Liste de pose listener a mettre � jour a chaque fin de parcours
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
		if(this.area.getCurrentArea().getId() != 15 && Main.USE_AREA){
			areaPositionUpdate();
		}
		float dist = mapPositionUpdate();
		if(Main.USE_AREA && this.area.getCurrentArea().getId() == 15 && checkRadarConsistancy()){
			this.area.updateArea();
		}
		
		return dist;
	}
	
	/**
	 * Mets � jour la position en fonction des donn�es de la zone actuelle
	 * @return vrai si l'area est coherente avec la position, faux sinon (demande de mise � jour de l'area)
	 */
	private boolean areaPositionUpdate() {
		float[] borders = this.area.getCurrentArea().getBorder();
		Pose myPose = this.odometryPoseProvider.getPose();
		
		float x = myPose.getX();
		float y = myPose.getY();
		
		// si minX est plus grand que le x actuel
		if(borders[0] > myPose.getX()){
			x = (1 - AREA_PERCENT) * x + (AREA_PERCENT) * borders[0];
		}
		
		// si x est plus grand que maxX
		if(borders[1] < myPose.getX()){
			x = (1 - AREA_PERCENT) * x + (AREA_PERCENT) * borders[1];
		}
		
		if(borders[2] > myPose.getY()){
			y = (1 - AREA_PERCENT) * y + (AREA_PERCENT) * borders[2];
		}
		
		if(borders[3] < myPose.getY()){
			y = (1 - AREA_PERCENT) * y + (AREA_PERCENT) * borders[3];
		}
		
		myPose.setLocation(x, y);
		if(this.odometryPoseProvider.getPose().distanceTo(myPose.getLocation()) < AREA_LOST){
			this.odometryPoseProvider.setPose(myPose);
			return true;
		}
		return false;
		
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

	synchronized public void setPose(Pose p) {
		this.odometryPoseProvider.setPose(p);
		broadcastPose();
		mapPositionUpdate();
		broadcastPose();
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
	synchronized public void addPoseListener(PoseListener pl){
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
	 * @param hasBeenInterrupted vrai si le deplacement a ete interrompu, faux sinon
	 */
	synchronized public void endLine(boolean hasBeenInterrupted){
		Main.log("[POSITION CALCULATOR]   : arrivee sur la position estimee : " + this.odometryPoseProvider.getPose().toString());
		
		broadcastPose();
		float dist = updatePose();
		broadcastPose();
		
		Main.printf("[POSITION CALCULATOR]   : (linear) Position fixee : " + this.odometryPoseProvider.getPose().toString());
			
		if(!hasBeenInterrupted){
			float errorDistance = getPosition().distanceTo(this.estimatedDest);
			if(errorDistance > MAX_SAMPLE_ERROR && !Main.PRESSION || dist > MAX_SAMPLE_ERROR * 2){
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
