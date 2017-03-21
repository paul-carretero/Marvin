package goals;


import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class GoalGrabPessimist extends Goal {
	
	protected final String 		NAME 	= "GoalGrab";
	protected 		Point		pallet 	= null;
	protected 		PoseGiver	pg 		= null;

	public GoalGrabPessimist(Marvin ia, int timeout, Point pallet, PoseGiver pg) {
		super(ia, timeout);
		
		this.pallet	= pallet;
		this.pg 	= pg;
	}

	@Override
	protected void defineDefault() {
		preConditions.add(Main.CALIBRATED);
		
		postConditions.add(Main.HAVE_PALET);
	}
	
	protected void correctPosition(){
		Pose currentPose = pg.getPosition();

		int distance = (int)currentPose.distanceTo(pallet);
		int angleCorrection = (int)currentPose.relativeBearing(pallet);
		
		ia.turnHere(angleCorrection, Main.ROTATION_SPEED);
		
		if(distance < Main.RADAR_MIN_RANGE){
			ia.goBackward(Main.RADAR_MIN_RANGE - distance + 100, Main.CRUISE_SPEED);
		}
		else if(distance > Main.RADAR_MAX_RANGE){
			ia.goForward(distance - Main.RADAR_MAX_RANGE + 100, Main.CRUISE_SPEED);
		}
	}
	
	protected boolean tryGrab(){
		if(Main.getState(Main.PRESSION)){
			ia.grab();
			Main.setState(Main.HAVE_PALET,true);
			return true;
		}
		return false;
	}
	
	@Override
	public void start() {
		// faudra aussi vérifier que le pallet est sur la mastertable...
		Main.printf("Here I am, brain the size of a planet, and they ask me to pick up a piece of paper.");
		
		correctPosition();
		
		int radarDistance 	= 9999;
		int previousRadar 	= 9999;
		boolean continuer	= true;
		int i 				= 0;
		
		ia.turnHere(-20,Main.ROTATION_SPEED);
		
		while(i < 4 && continuer){
			radarDistance = pg.getRadarDistance();
			ia.turnHere(10,Main.ROTATION_SPEED);
			if(previousRadar < radarDistance){
				continuer = false;
			}
			previousRadar = radarDistance;
			i++;
		}
				
		if(!continuer){ // on a trouver un plus grand, c'étais donc le pas d'avant
			ia.turnHere(-10, Main.ROTATION_SPEED);
		}

		Pose currentPose 	= pg.getPosition();
		int distance 		= (int)currentPose.distanceTo(pallet);
		radarDistance 	= pg.getRadarDistance();
		
		if(radarDistance < Main.RADAR_MAX_RANGE  && Main.areApproximatlyEqual(radarDistance,distance,700) ){
			ia.goForward(distance+100, Main.CRUISE_SPEED);
			if(!tryGrab()){
				failGrabHandler();
			}
		}
	}

	protected void failGrabHandler() {
		ia.goBackward(150, Main.CRUISE_SPEED);
		ia.turnHere(12, Main.ROTATION_SPEED);
		ia.goForward(150, Main.CRUISE_SPEED);
		
		if(!tryGrab()){
			ia.goBackward(150, Main.CRUISE_SPEED);
			ia.turnHere(-24, Main.ROTATION_SPEED);
			ia.goForward(150, Main.CRUISE_SPEED);
			tryGrab();
		}
	}

	@Override
	public void preConditionsFailHandler() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return NAME;
	}
}
