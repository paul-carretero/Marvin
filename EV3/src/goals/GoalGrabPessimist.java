package goals;


import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

public class GoalGrabPessimist extends Goal {
	
	protected final String 		NAME 	= "GoalGrab";
	protected 		Point		pallet 	= null;
	protected 		PoseGiver	pg 		= null;
	protected		ItemGiver	eom		= null;

	public GoalGrabPessimist(GoalFactory gf, Marvin ia, int timeout, Point pallet, PoseGiver pg, ItemGiver eom) {
		super(gf, ia, timeout);
		
		this.eom 	= eom;
		this.pallet	= pallet;
		this.pg 	= pg;
	}

	@Override
	protected void defineDefault() {
		postConditions.add(Main.HAVE_PALET);
	}
	
	protected void correctPosition(){
		Pose currentPose = pg.getPosition();

		int distance = (int)currentPose.distanceTo(pallet);
		int angleCorrection = (int)currentPose.relativeBearing(pallet);
		
		System.out.println("currentPose = " + currentPose.toString());
		System.out.println("Objective = " + pallet.toString());
		System.out.println("distance = "+ distance);
		System.out.println("angle = " + angleCorrection);
		
		ia.turnHere(angleCorrection);
		
		if(distance < Main.RADAR_MIN_RANGE){
			ia.goBackward(Main.RADAR_MIN_RANGE - distance + 100);
		}
		else if(distance > Main.RADAR_MAX_RANGE){
			ia.goForward(distance - Main.RADAR_MAX_RANGE + 100);
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
		if(eom.checkPallet(new IntPoint(pallet.x, pallet.y))){
			correctPosition();
			
			int radarDistance 	= 9999;
			int previousRadar 	= 9999;
			boolean continuer	= true;
			int i 				= 0;
			
			ia.turnHere(-20);
			
			while(i < 4 && continuer){
				radarDistance = pg.getRadarDistance();
				Main.printf("radar = " + radarDistance);
				ia.turnHere(10);
				if(previousRadar < radarDistance){
					continuer = false;
				}
				previousRadar = radarDistance;
				i++;
			}
			
			if(!continuer){ // on a trouver un plus grand, c'étais donc le pas d'avant
				ia.turnHere(-10);
			}
	
			Pose currentPose 	= pg.getPosition();
			int distance 		= (int)currentPose.distanceTo(pallet);
			radarDistance 	= pg.getRadarDistance();
			
			ia.setAllowInterrupt(true);
			
			if(radarDistance < Main.RADAR_MAX_RANGE  && Main.areApproximatlyEqual(radarDistance,distance,700) ){
				ia.goForward(distance);
				if(!tryGrab()){
					failGrabHandler();
				}
			}
		}
	}

	protected void failGrabHandler() {
		ia.goBackward(180);
		ia.turnHere(15);
		ia.goForward(180);
		
		if(!tryGrab()){
			ia.goBackward(180);
			ia.turnHere(-30);
			ia.goForward(180);
			tryGrab();
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}
