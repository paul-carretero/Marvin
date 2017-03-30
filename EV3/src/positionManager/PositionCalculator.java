package positionManager;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.ModeListener;
import interfaces.PoseGiver;
import interfaces.SignalListener;
import shared.IntPoint;
import shared.Mode;
import shared.SignalType;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class PositionCalculator extends Thread implements ModeListener, PoseGiver {

	private static final float		MAP_PERCENT			= 0.3f;
	private static final float		RADAR_PERCENT		= 0.15f;
	private static final float		AREA_PERCENT		= 0.3f;
	private static final int 		REFRESH_RATE		= 400;
	private static final int		MAX_SAMPLE_ERROR	= 180;
	
	private volatile Mode			mode;
	private DistanceGiver 			radar;
	private OdometryPoseProvider 	odometryPoseProvider;
	private DirectionCalculator 	directionCalculator;
	private ItemGiver				eom;
	private AreaGiver 				area;
	private SignalListener			marvin;
	
	public PositionCalculator(MoveProvider mp, DirectionCalculator directionCalculator, DistanceGiver radar, SignalListener ia){
		this.mode 					= Mode.ACTIVE;
		this.radar 					= radar;
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.directionCalculator	= directionCalculator;
		this.marvin					= ia;
		
		odometryPoseProvider.setPose(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
		
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	public void addItemGiver(ItemGiver eom){
		this.eom = eom;
	}
	
	public void addAreaManager(AreaGiver area){
		this.area = area;
	}
	
	@Override
	public void run() {
		Main.printf("[POSITION CALCULATOR]   : Started");
		while(!isInterrupted() && mode != Mode.END){
			updatePose();
			if(!checkConsistancy()){
				marvin.signal(SignalType.LOST);
			}
			Main.printf("[POSITION CALCULATOR]   : " + odometryPoseProvider.getPose().toString());
			//Main.printf("[POSITION CALCULATOR]   : Radar : " + radar.getNearItemDistance());
			syncWait();
		}
		Main.printf("[POSITION CALCULATOR]   : Finished");
		
	}
	
	private void updatePose() {
		
		// mise à jour de la position
		radarPositionUpdate(); // 15%
		mapPositionUpdate(); // 30%
		AreaPositionUpdate(); // 30%
		
		// mise à jour de l'angle
		Pose pose = odometryPoseProvider.getPose();
		directionCalculator.updateAngle(pose);
		odometryPoseProvider.setPose(pose);
	}
	
	// radarDistance = différence entre le position carte et la position radar
	private boolean checkConsistancy(){
		int nCorrect = 1;
		
		// radar pas super fiable
		if(checkRadarConsistancy()){
			nCorrect += 1;
		}
		
		// peut se tromper car capteur pas super fiable
		if(area.getCurrentArea().getConsistency(odometryPoseProvider.getPose())){
			nCorrect += 2;
		}
		if(checkMapConsistancy()){
			nCorrect += 3;
		}
		
		// si on est plutôt sur de la ou on est alors on met à jour les areas si besoin
		if(area.getCurrentArea().getId() == 15 && nCorrect > 6){
			area.updateArea();
		}
		
		// on est sur de la ou on est avec au moins 2 ou 3 de cohérent
		return nCorrect > 3;
	}
	
	private boolean checkRadarConsistancy() {
		int radarDistance = radar.getRadarDistance();
		if(radarDistance < Main.RADAR_MAX_RANGE && radarDistance > Main.RADAR_MIN_RANGE){
		
			Pose tempPose = odometryPoseProvider.getPose();
			tempPose.moveUpdate(radarDistance);
			
			// si on a pas detecter un mur... avec 3 cm de marge d'erreur
			if(tempPose.getX() > 30 && tempPose.getX() < 1970 && tempPose.getY() < 2970 && tempPose.getY() > 30){
				Point bestMatch = eom.getNearestItem(new IntPoint(tempPose.getLocation())).toLejosPoint();
				
				tempPose = odometryPoseProvider.getPose();
				
				// on ne corrige que de 15% parceque le radar n'est pas fiable, dans le direction de l'item detecté
				return Math.abs(tempPose.distanceTo(bestMatch) - radarDistance) < MAX_SAMPLE_ERROR;
			}
			else{
				// il y avait un mur alors on ne sait pas
				return true;
			}
		}
		return false;
	}

	private boolean checkMapConsistancy() {
		IntPoint me = eom.getMarvinPosition();
		
		// on vérifie si la distance entre le position de la map et celle de l'odomètre est bien infèrieur à 20cm
		if(me != null){
			return me.getDistance(new IntPoint(odometryPoseProvider.getPose())) < MAX_SAMPLE_ERROR;
		}
		return false;
	}

	private void AreaPositionUpdate() {
		float[] borders = area.getCurrentArea().getBorder();
		Pose myPose = odometryPoseProvider.getPose();
		
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
		odometryPoseProvider.setPose(myPose);
	}

	private void mapPositionUpdate() {
		IntPoint me = eom.getMarvinPosition();
		
		Pose myPose = odometryPoseProvider.getPose();
		
		float x = (float) (me.x()) * (MAP_PERCENT) + myPose.getX() * (1 - MAP_PERCENT);
		float y = (float) (me.y()) * (MAP_PERCENT) + myPose.getY() * (1 - MAP_PERCENT);
		
		myPose.setLocation(x, y);
		odometryPoseProvider.setPose(myPose);
	}

	public void syncWait(){
		synchronized (this) {
			try {
				this.wait(REFRESH_RATE);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void setMode(Mode m) {
		this.mode = m;
	}
	
	public void swap(){
		Pose current = odometryPoseProvider.getPose();
		current.setHeading(current.getHeading()-180);
		odometryPoseProvider.setPose(current);
	}

	public Pose getPosition() {
		return odometryPoseProvider.getPose();
	}

	public void sendFixX(int x) {
		Pose tempPose = odometryPoseProvider.getPose();
		
		Main.poseRealToSensor(tempPose);
		
		tempPose.setLocation(x, tempPose.getY());
		
		Main.poseSensorToReal(tempPose);
		
		//odometryPoseProvider.setPose(tempPose);
	}

	public void sendFixY(int y) {
		Pose tempPose = odometryPoseProvider.getPose();
		
		Main.poseRealToSensor(tempPose);
		
		tempPose.setLocation(tempPose.getX(), y);
		
		Main.poseSensorToReal(tempPose);
		
		//odometryPoseProvider.setPose(tempPose);
	}
	
	//a appeler en premier car retourne le première pose
	private void radarPositionUpdate(){
		int radarDistance = radar.getRadarDistance();
		if(radarDistance < Main.RADAR_MAX_RANGE && radarDistance > Main.RADAR_MIN_RANGE){
		
			Pose tempPose = odometryPoseProvider.getPose();
			tempPose.moveUpdate(radarDistance);
			
			// si on a pas detecter un mur... avec 3 cm de marge d'erreur
			if(tempPose.getX() > 30 && tempPose.getX() < 1970 && tempPose.getY() < 2970 && tempPose.getY() > 30){
				Point bestMatch = eom.getNearestItem(new IntPoint(tempPose.getLocation())).toLejosPoint();
				
				tempPose = odometryPoseProvider.getPose();
				
				float realHeading = tempPose.getHeading();
				float headingToBestMatch = tempPose.angleTo(bestMatch);
				tempPose.setHeading(headingToBestMatch);
				
				// on ne corrige que de 15% parceque le radar n'est pas fiable, dans le direction de l'item detecté
				tempPose.moveUpdate((tempPose.distanceTo(bestMatch) - radarDistance) * (RADAR_PERCENT));
				
				tempPose.setHeading(realHeading);
				
				odometryPoseProvider.setPose(tempPose);
			}
		}
	}

	public int getAreaId() {
		return area.getCurrentArea().getId();
	}
}
