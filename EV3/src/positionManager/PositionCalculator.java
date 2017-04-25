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
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Move.MoveType;

/**
 * Class principale centralisant les informations de position. re�oit ou les informations de d�placement et calcule la coh�rence en fonction des capteurs
 */
public class PositionCalculator implements PoseGiver, MoveListener {

	/**
	 * Pourcentage de la position de la carte qui sera utilis� pour calculer la position final une fois le mouvement arr�t�
	 */
	private static final float		MAP_PERCENT			= 0.7f;
	
	/**
	 *  Pourcentage de la position du areaManager qui sera utilis� pour calculer la position final une fois le mouvement arr�t�
	 */
	private static final float		AREA_PERCENT		= 0.1f;
	
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
	 * Vrai si le robot circule en marche arriere
	 */
	private volatile boolean 		setBackward = false;
	
	/**
	 * Vrai si le mouvement du Robot a ete interrompu, faux sinon
	 */
	private volatile boolean		hasBeenInterrupted = false;
	
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
	public void addPoseListener(PoseListener pl){
		this.poseListeners.add(pl);
	}
	
	/**
	 * Envoit a tous les PoseListener la derni�re position de la pose
	 */
	private void broadcastPose(){
		Pose mypose = this.odometryPoseProvider.getPose();
		for(PoseListener p : this.poseListeners){
			p.setPose(mypose);
		}
	}
	
	/**
	 * Tente de mettre � jour la pose (position seulement) du robot en fonction des informations re�ues
	 * Met eventuellement � jour l'area
	 */
	private void updatePose() {
		if(this.area.getCurrentArea().getId() != 15){
			areaPositionUpdate();
		}
		mapPositionUpdate();
		if(this.area.getCurrentArea().getId() == 15 && checkRadarConsistancy()){
			this.area.updateArea();
		}
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
	 */
	synchronized private void mapPositionUpdate() {
		IntPoint me = this.eom.getMarvinPosition();
		Pose myPose = this.odometryPoseProvider.getPose();
		
		if(me != null){
			
			Main.poseRealToSensor(myPose);
			
			float x = me.x() * (MAP_PERCENT) + myPose.getX() * (1 - MAP_PERCENT);
			float y = me.y() * (MAP_PERCENT) + myPose.getY() * (1 - MAP_PERCENT);
			
			myPose.setLocation(x, y);
			
			Main.poseSensorToReal(myPose);
			
			this.odometryPoseProvider.setPose(myPose);
		}
	}

	synchronized public Pose getPosition() {
		return this.odometryPoseProvider.getPose();
	}

	synchronized public void setPose(Pose p) {
		this.odometryPoseProvider.setPose(p);
		broadcastPose();
		mapPositionUpdate();
		broadcastPose();
	}

	synchronized public void moveStarted(Move event, MoveProvider mp) {
		Main.log("[POSITION CALCULATOR]   : depart sur : " + this.odometryPoseProvider.getPose().toString());
	}
	
	/**
	 * on ne met � jour le position que si l'on a effectu� un d�placement de type TRAVEL (lin�aire)
	 * si on est en marche arriere alors l'ia nous indiquera notre position
	 * @param distance distance parcouru sur la ligne
	 */
	synchronized public void startLine(final float distance) {
		Pose myCurrentPose = this.odometryPoseProvider.getPose();
		myCurrentPose.moveUpdate(distance);
		this.estimatedDest = myCurrentPose.getLocation();
		Main.log("[POSITION CALCULATOR]   : a l'arret, position future estimee : " + this.estimatedDest);
	}

	synchronized public void moveStopped(Move event, MoveProvider mp) {
		Main.log("[POSITION CALCULATOR]   : arrivee sur la position estimee : " + this.odometryPoseProvider.getPose().toString());
		broadcastPose();
		if(!this.setBackward){
			if (event.getMoveType() == MoveType.TRAVEL){
				updatePose();
			}
			broadcastPose();
			Main.printf("[POSITION CALCULATOR]   : Position fixee : " + this.odometryPoseProvider.getPose().toString());
			
			if(this.estimatedDest != null && !this.hasBeenInterrupted){
				float errorDistance = getPosition().distanceTo(this.estimatedDest);
				if(errorDistance > MAX_SAMPLE_ERROR && !Main.PRESSION){
					this.marvin.signalLost();
				}
				this.estimatedDest = null;
			}
		}
		this.hasBeenInterrupted = false;
		broadcastPose();
	}
	
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

	/**
	 * @param b vrai si le robot se d�placera en marche arriere, faux sinon
	 */
	synchronized public void setBackward(boolean b) {
		this.setBackward = b;
	}
	
	/**
	 * @param b vrai si le robot a ete interrompu dans sa course par un obstacle ou un mur par exemple
	 */
	synchronized public void setInterrupted(boolean b) {
		this.hasBeenInterrupted = b;
	}
}
