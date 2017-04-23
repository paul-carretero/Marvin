package positionManager;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
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
 * Class principale centralisant les informations de position. reçoit ou les informations de déplacement et calcule la cohérence en fonction des capteurs
 */
public class PositionCalculator extends Thread implements PoseGiver, MoveListener {

	/**
	 * Pourcentage de la position de la carte qui sera utilisé pour calculer la position final une fois le mouvement arrété
	 */
	private static final float		MAP_PERCENT			= 0.5f;
	
	/**
	 * Pourcentage de la position de la carte qui sera utilisé pour calculer la position final lorsque le mouvement est en cours
	 */
	private static final float		CONTINUOUS_PERCENT	= 0.2f;
	
	/**
	 *  Pourcentage de la position du areaManager qui sera utilisé pour calculer la position final une fois le mouvement arrété
	 */
	private static final float		AREA_PERCENT		= 0.5f;
	
	/**
	 * Distance maximum entre le point donné et le point calculé (en mm) avant qu'un gestionnaire de position se déclare en état de "lost".
	 */
	private static final int		MAX_SAMPLE_ERROR	= 250;
	
	/**
	 * Temps entre deux mise à jour du mouvmeent en cours
	 */
	private static final int 		REFRESHRATE			= 500;
	
	/**
	 * Temps entre deux mise à jour du mouvmeent en cours
	 */
	private static final int 		MIN_DIST_CONS_CHECK	= 230;
	
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
	private boolean					lost;
	
	/**
	 * Vrai si le robot avance en ligne droite en avant, faux sinon.
	 */
	private volatile boolean		isMovingForward;
	
	/**
	 * Initialise les principaux paramètre initiaux du gestionnaire de position.
	 * @param mp le pilot du robot (de la librairie LeJos)
	 * @param radar Une instance du radar du robot
	 * @param ia le gestionnaire de l'IA et des objectifs
	 */
	public PositionCalculator(final MoveProvider mp, final DistanceGiver radar, final SignalListener ia){
		super("PositionCalculator");
		this.radar 					= radar;
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.marvin					= ia;
		this.lost					= false;
		this.isMovingForward		= false;
		
		initPose();
		
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	@Override
	public void run(){
		Main.printf("[POSITION CALCULATOR]   : Started");
		this.setPriority(MAX_PRIORITY);
		while(!isInterrupted()){
			
			if(this.isMovingForward){
				mapPositionUpdate(CONTINUOUS_PERCENT);
			}
			
			syncWait();
		}
		Main.printf("[POSITION CALCULATOR]   : FInished");
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
	 */
	private void updatePose() {
		mapPositionUpdate(MAP_PERCENT);
		AreaPositionUpdate();
	}
	
	/**
	 * Vérifie la cohérence des données de navigation, notament utilisé pour détecter une perte du robot.
	 * @return vrai si les données de position sont cohérente, faux sinon
	 */
	private boolean checkConsistancy(){
		int nCorrect = 1;
				
		// radar pas super fiable
		// surtout pour l'angle
		if(checkRadarConsistancy()){
			nCorrect += 1;
		}
				
		// peut se tromper car capteur pas super fiable
		if(this.area.getCurrentArea().getConsistency(this.odometryPoseProvider.getPose())){
			nCorrect += 2;
		}
				
		if(checkMapConsistancy()){
			nCorrect += 3;
		}
				
		// si on est plutôt sur de la ou on est alors on met à jour les areas si besoin
		if(this.area.getCurrentArea().getId() == 15 && nCorrect > 6){
			this.area.updateArea();
		}
				
		// on est sur de la ou on est avec au moins 2 ou 3 de cohérent
		return nCorrect > 3;
	}
	
	/**
	 * Utilisé pour détecter une perte en fonction des données radar, le radar n'est toutefois pas vraiment fiable...
	 * @return vrai si la position actuelle est cohérente avec les données radar (en fonction de l'item que l'on a devant), faux sinon
	 */
	private boolean checkRadarConsistancy() {
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
	 * Utilisé pour détecter une perte en fonction des données de la carte des Item
	 * @return vrai si la position actuelle est cohérente avec les données de la carte, faux sinon
	 */
	private boolean checkMapConsistancy() {
		IntPoint me = this.eom.getMarvinPosition();
		
		if(me != null){
			Main.printf("Map consistency : me = " + me + " && MyPose = " + this.odometryPoseProvider.getPose());
			return me.getDistance(new IntPoint(this.odometryPoseProvider.getPose())) < MAX_SAMPLE_ERROR;
			
		}
		return false;
	}

	/**
	 * Mets à jour la position en fonction des données de la zone actuelle
	 */
	private void AreaPositionUpdate() {
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
		this.odometryPoseProvider.setPose(myPose);
	}

	/**
	 * Mets à jour la position en fonction des données de l'item le plus proche sur la map.
	 * @param percent pourcentage de correction de la pose donnée par l'odomètre
	 */
	synchronized private void mapPositionUpdate(float percent) {
		IntPoint me = this.eom.getMarvinPosition();
		
		Pose myPose = this.odometryPoseProvider.getPose();
		
		if(me != null){
			
			// Main.poseRealToSensor(myPose); // UNUSED (théoriquement utile si tout les capteurs étaient super précis...)
			
			float x = me.x() * (percent) + myPose.getX() * (1 - percent);
			float y = me.y() * (percent) + myPose.getY() * (1 - percent);
			
			myPose.setLocation(x, y);
			
			//Main.poseSensorToReal(myPose); // UNUSED idem...
			
			this.odometryPoseProvider.setPose(myPose);
		}
	}

	/**
	 * Permet d'attendre pendant une durée définie.
	 */
	synchronized private void syncWait(){
		try {
			this.wait(REFRESHRATE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Change l'angle du robot en fonction de la direction, si on avance en marche arrière alors l'angle est inverse (-180°).
	 * Pas géré par la bibliothèque LeJos sinon...
	 */
	synchronized public void swap(){
		Pose current = this.odometryPoseProvider.getPose();
		float currentHeading = current.getHeading();
		if(currentHeading > 0){
			currentHeading = currentHeading - 180;
		}
		else{
			currentHeading = currentHeading + 180;
		}
		
		current.setHeading(currentHeading);
		this.odometryPoseProvider.setPose(current);
	}

	public Pose getPosition() {
		return this.odometryPoseProvider.getPose();
	}

	synchronized public void sendFixX(int x) {
		Pose tempPose = this.odometryPoseProvider.getPose();
		
		if(Math.abs(x - tempPose.getX()) < MAX_SAMPLE_ERROR){
			Main.poseRealToSensor(tempPose);
			tempPose.setLocation(x, tempPose.getY());
			Main.poseSensorToReal(tempPose);
			this.odometryPoseProvider.setPose(tempPose);
		}
	}

	synchronized public void sendFixY(int y) {
		Pose tempPose = this.odometryPoseProvider.getPose();
		
		if(Math.abs(y - tempPose.getY()) < MAX_SAMPLE_ERROR){
			Main.poseRealToSensor(tempPose);
			tempPose.setLocation(tempPose.getX(), y);
			Main.poseSensorToReal(tempPose);
			this.odometryPoseProvider.setPose(tempPose);
		}
	}

	synchronized public int getAreaId() {
		return this.area.getCurrentArea().getId();
	}

	public void setPose(Pose p) {
		this.odometryPoseProvider.setPose(p);
	}

	public void moveStarted(Move event, MoveProvider mp) {
		// void
	}

	synchronized public void moveStopped(Move event, MoveProvider mp) {
		
		// on ne met à jour le position que si l'on a effectué un déplacement de type TRAVEL (linéaire)
		if (event.getMoveType() == MoveType.TRAVEL){
			
			updatePose();
			
			// si on a parcouru une distance pas trop petite alors on vérifie si on est toujours consistent avec notre position
			
			if(event.getDistanceTraveled() > MIN_DIST_CONS_CHECK){
				if (!checkConsistancy()){
					if(!this.lost){
						this.marvin.signalLost();
						this.lost = true;
					}
				}
				else if(this.lost){
					this.marvin.signalNoLost();
					this.lost = false;
				}
			}
		}
		
				
		
				
		//Main.printf("[POSITION CALCULATOR]   : " + this.odometryPoseProvider.getPose().toString());
		//Main.printf("[POSITION CALCULATOR]   : Radar : " + this.radar.getRadarDistance());
		
	}

	/**
	 * Permet de spécifier si le robot avance en avant, utile afin de mettre à jour de manière fréquente la position en fonction de la carte des item.
	 * @param b vrai si le robot avance en avant, faux sinon.
	 */
	public void setIsMovingForward(boolean b) {
		this.isMovingForward = b;
	}
}
