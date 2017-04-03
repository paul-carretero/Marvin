package positionManager;

import aiPlanner.Main;
import interfaces.AreaGiver;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import interfaces.SignalListener;
import shared.IntPoint;
import shared.SignalType;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class PositionCalculator extends Thread implements PoseGiver {

	private static final float		MAP_PERCENT			= 0.2f;
	private static final float		RADAR_PERCENT		= 0.05f;
	private static final float		AREA_PERCENT		= 0.2f;
	private static final int		MAX_SAMPLE_ERROR	= 180;
	
	private DistanceGiver 			radar;
	private OdometryPoseProvider 	odometryPoseProvider;
	private DirectionCalculator 	directionCalculator;
	private ItemGiver				eom;
	private AreaGiver 				area;
	private SignalListener			marvin;
	private int 					refreshRate			= 400;
	private boolean					lost;
	
	public PositionCalculator(MoveProvider mp, DirectionCalculator directionCalculator, DistanceGiver radar, SignalListener ia){
		this.radar 					= radar;
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.directionCalculator	= directionCalculator;
		this.marvin					= ia;
		this.lost					= false;
		
		this.odometryPoseProvider.setPose(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
		
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
		this.setPriority(MAX_PRIORITY);
		while(!isInterrupted()){
			updatePose();
			if(!checkConsistancy()){
				if(!this.lost){
					this.marvin.signal(SignalType.LOST);
				}
				this.lost = true;
			}
			else{
				
				/*
				 *  si j'étais perdu et que je me suis retouvé alors je demande à l'ia de supprimer les objectif de recalibration.
				 *  L'objectif éventuellement en cours se poursuivra de toute façon
				 */
				if(this.lost){
					this.marvin.signal(SignalType.NO_LOST);
				}
				
				this.lost = false;
			}
			Main.printf("[POSITION CALCULATOR]   : " + this.odometryPoseProvider.getPose().toString());
			//Main.printf("[POSITION CALCULATOR]   : Radar : " + radar.getRadarDistance());
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
		Pose pose = this.odometryPoseProvider.getPose();
		this.directionCalculator.updateAngle(pose);
		this.odometryPoseProvider.setPose(pose);
	}
	
	// radarDistance = différence entre le position carte et la position radar
	private boolean checkConsistancy(){
		int nCorrect = 1;
		
		// radar pas super fiable
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
					
					// on ne corrige que de 15% parceque le radar n'est pas fiable, dans le direction de l'item detecté
					return Math.abs(tempPose.distanceTo(bestMatch) - radarDistance) < MAX_SAMPLE_ERROR;
				}
				return true;
			}
			// il y avait un mur alors on ne sait pas
			return true;
		}
		return false;
	}

	private boolean checkMapConsistancy() {
		IntPoint me = this.eom.getMarvinPosition();
		
		// on vérifie si la distance entre le position de la map et celle de l'odomètre est bien infèrieur à 20cm
		if(me != null){
			return me.getDistance(new IntPoint(this.odometryPoseProvider.getPose())) < MAX_SAMPLE_ERROR;
		}
		return false;
	}

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

	private void mapPositionUpdate() {
		IntPoint me = this.eom.getMarvinPosition();
		
		Pose myPose = this.odometryPoseProvider.getPose();
		
		float x = me.x() * (MAP_PERCENT) + myPose.getX() * (1 - MAP_PERCENT);
		float y = me.y() * (MAP_PERCENT) + myPose.getY() * (1 - MAP_PERCENT);
		
		myPose.setLocation(x, y);
		this.odometryPoseProvider.setPose(myPose);
	}

	synchronized public void syncWait(){
		try {
			this.wait(this.refreshRate);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void swap(){
		Pose current = this.odometryPoseProvider.getPose();
		current.setHeading(current.getHeading()-180);
		this.odometryPoseProvider.setPose(current);
	}

	public Pose getPosition() {
		return this.odometryPoseProvider.getPose();
	}

	public void sendFixX(int x) {
		Pose tempPose = this.odometryPoseProvider.getPose();
		
		Main.poseRealToSensor(tempPose);
		
		tempPose.setLocation(x, tempPose.getY());
		
		Main.poseSensorToReal(tempPose);
		
		//odometryPoseProvider.setPose(tempPose);
	}

	public void sendFixY(int y) {
		Pose tempPose = this.odometryPoseProvider.getPose();
		
		Main.poseRealToSensor(tempPose);
		
		tempPose.setLocation(tempPose.getX(), y);
		
		Main.poseSensorToReal(tempPose);
		
		//odometryPoseProvider.setPose(tempPose);
	}
	
	//a appeler en premier car retourne le première pose
	private void radarPositionUpdate(){
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
					
					float realHeading = tempPose.getHeading();
					float headingToBestMatch = tempPose.angleTo(bestMatch);
					tempPose.setHeading(headingToBestMatch);
					
					// on ne corrige que de 5% parceque le radar n'est pas fiable, dans le direction de l'item detecté
					tempPose.moveUpdate((tempPose.distanceTo(bestMatch) - radarDistance) * (RADAR_PERCENT));
					
					tempPose.setHeading(realHeading);
					
					this.odometryPoseProvider.setPose(tempPose);
				}
			}
		}
	}

	public int getAreaId() {
		return this.area.getCurrentArea().getId();
	}

	public void setPose(Pose p) {
		this.odometryPoseProvider.setPose(p);
	}
}
