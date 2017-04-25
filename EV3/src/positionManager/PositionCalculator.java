package positionManager;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.SignalListener;
import shared.Color;
import shared.IntPoint;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Move.MoveType;

/**
 * Class principale centralisant les informations de position. reçoit ou les informations de déplacement et calcule la cohérence en fonction des capteurs
 */
public class PositionCalculator implements PoseGiver, MoveListener {

	/**
	 * Pourcentage de la position de la carte qui sera utilisé pour calculer la position final une fois le mouvement arrété
	 */
	private static final float		MAP_PERCENT			= 0.7f;
	
	/**
	 *  Pourcentage de la position du areaManager qui sera utilisé pour calculer la position final une fois le mouvement arrété
	 */
	private static final float		AREA_PERCENT		= 0.1f;
	
	/**
	 * Distance au dela de laquelle on considère l'area comme invalidee
	 */
	private static final float		AREA_LOST			= 300f;
	
	/**
	 * Distance maximum entre le point donné et le point calculé (en mm) avant qu'un gestionnaire de position se déclare en état de "lost".
	 */
	private static final int		MAX_SAMPLE_ERROR	= 300;
	
	/**
	 * Instance du radar permettant de retourne la distance vers un objet situé devant le robot
	 */
	private final DistanceGiver 	radar;
	
	/**
	 * Pose provider fourni par la librairie LeJos, utilisé comme base pour suivre les déplacement du robot
	 */
	private final OdometryPoseProvider odometryPoseProvider;
	
	/**
	 * Classe centralisant les données de type IA, notament les interruption ou les demandes de navigation
	 */
	private final SignalListener	marvin;
	
	/**
	 * Interface retournant la position des item sur la carte, dont le robot
	 */
	private ItemGiver				eom;
	
	/**
	 * Classe donnant l'area actuelle sur laquelle est le robot.
	 */
	private AreaGiver 				area;
	
	/**
	 * Vrai si le positionManager se considère comme perdu, faux sinon
	 */
	private Point					estimatedDest;

	/**
	 * Vrai si le robot circule en marche arriere
	 */
	private boolean 				setBackward = false;
	
	/**
	 * Vrai si le mouvement du Robot a ete interrompu, faux sinon
	 */
	private volatile boolean		hasBeenInterrupted = false;
	
	/**
	 * Initialise les principaux paramètre initiaux du gestionnaire de position.
	 * @param mp le pilot du robot (de la librairie LeJos)
	 * @param radar Une instance du radar du robot
	 * @param ia le gestionnaire de l'IA et des objectifs
	 */
	public PositionCalculator(final MoveProvider mp, final DistanceGiver radar, final SignalListener ia){
		this.radar 					= radar;
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.marvin					= ia;
		
		initPose();
		
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	/**
	 * Définie la pose actuelle du robot comme la pose initiale au commencement du jeu
	 */
	public final void initPose(){
		this.odometryPoseProvider.setPose(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
	}
	
	/**
	 * @param eom un ItemGiver fournissant les données reçue par le serveur
	 */
	synchronized public void addItemGiver(final ItemGiver eom){
		this.eom = eom;
	}
	
	/**
	 * @param area le gestionnaire des Area
	 */
	synchronized public void addAreaManager(final AreaGiver area){
		this.area = area;
	}
	
	/**
	 * Tente de mettre à jour la pose (position seulement) du robot en fonction des informations reçues
	 * Met eventuellement à jour l'area
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
	 * Mets à jour la position en fonction des données de la zone actuelle
	 * @return vrai si l'area est coherente avec la position, faux sinon (demande de mise à jour de l'area)
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
	 * Mets à jour la position en fonction des données de l'item le plus proche sur la map.
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

	synchronized public int getAreaId() {
		return this.area.getCurrentArea().getId();
	}

	synchronized public void setPose(Pose p) {
		this.odometryPoseProvider.setPose(p);
		mapPositionUpdate();
	}

	synchronized public void moveStarted(Move event, MoveProvider mp) {
		Main.printf("[POSITION CALCULATOR]   : start on : " + this.odometryPoseProvider.getPose().toString());
	}
	
	/**
	 * on ne met à jour le position que si l'on a effectué un déplacement de type TRAVEL (linéaire)
	 * si on est en marche arriere alors l'ia nous indiquera notre position
	 * @param distance distance parcouru sur la ligne
	 */
	synchronized public void startLine(final float distance) {
		Pose myCurrentPose = this.odometryPoseProvider.getPose();
		myCurrentPose.moveUpdate(distance);
		this.estimatedDest = myCurrentPose.getLocation();
		Main.printf("[POSITION CALCULATOR]   : estimated : " + this.estimatedDest);
	}

	synchronized public void moveStopped(Move event, MoveProvider mp) {
		Main.printf("[POSITION CALCULATOR]   : end on estimated : " + this.odometryPoseProvider.getPose().toString());
		
		if(!this.setBackward){
			if (event.getMoveType() == MoveType.TRAVEL){
				updatePose();
			}
			
			Main.printf("[POSITION CALCULATOR]   : end on fixed : " + this.odometryPoseProvider.getPose().toString());
			
			if(this.estimatedDest != null && !this.hasBeenInterrupted){
				float errorDistance = getPosition().distanceTo(this.estimatedDest);
				if(errorDistance > MAX_SAMPLE_ERROR && !Main.PRESSION){
					this.marvin.signalLost();
				}
				this.estimatedDest = null;
			}
		}
		this.hasBeenInterrupted = false;
	}
	
	/**
	 * Utilisé pour détecter une perte en fonction des données radar, le radar n'est toutefois pas vraiment fiable...
	 * @return vrai si la position actuelle est cohérente avec les données radar (en fonction de l'item que l'on a devant), faux sinon
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
	 * @param b vrai si le robot se déplacera en marche arriere, faux sinon
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
