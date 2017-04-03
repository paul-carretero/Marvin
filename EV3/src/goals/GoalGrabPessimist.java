package goals;


import aiPlanner.Main;
import aiPlanner.Marvin;
import interfaces.DistanceGiver;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.IntPoint;

public class GoalGrabPessimist extends Goal {
	
	protected final GoalType		NAME = GoalType.GRAB_PESSIMISTE;
	protected 		Point			pallet;
	protected 		PoseGiver		pg;
	protected		ItemGiver		eom;
	protected		DistanceGiver 	radar;

	public GoalGrabPessimist(GoalFactory gf, Marvin ia, Point pallet, PoseGiver pg, ItemGiver eom, DistanceGiver radar) {
		super(gf, ia);
		this.radar	= radar;
		this.eom 	= eom;
		this.pallet	= pallet;
		this.pg 	= pg;
	}

	protected void correctPosition(){
		Pose currentPose = this.pg.getPosition();

		int distance = (int)currentPose.distanceTo(this.pallet);
		int angleCorrection = (int)currentPose.relativeBearing(this.pallet);
		
		this.ia.turnHere(angleCorrection);
		
		if(distance < Main.RADAR_DEFAULT_RANGE){
			this.ia.goBackward(Main.RADAR_DEFAULT_RANGE - distance);
		}
		else if(distance > Main.RADAR_DEFAULT_RANGE){
			this.ia.goForward(distance - Main.RADAR_DEFAULT_RANGE);
		}
	}
	
	protected boolean tryGrab(){
		if(Main.getState(Main.PRESSION)){
			Main.setState(Main.HAVE_PALET,true);
			this.ia.grab();
			return true;
		}
		return false;
	}
	
	protected void updateStatus(){
		this.gf.setLastGrab(Main.getState(Main.HAVE_PALET));
	}
	
	@Override
	public void start() {
		this.ia.setResearchMode(true);
		if(this.eom.checkPallet(new IntPoint(this.pallet))){
			correctPosition();
			
			int radarDistance 	= Main.RADAR_OUT_OF_BOUND;
			int previousRadar 	= Main.RADAR_OUT_OF_BOUND;
			boolean continuer	= true;
			int i 				= 0;
			
			this.ia.turnHere(-30);
			
			while(i < 3 && continuer){
				radarDistance = this.radar.getRadarDistance();
				Main.printf("radar = " + radarDistance);
				this.ia.turnHere(30);
				if(previousRadar < radarDistance){
					continuer = false;
				}
				previousRadar = radarDistance;
				i++;
			}
			
			if(!continuer){ // on a trouver un plus grand, c'étais donc le pas d'avant
				this.ia.turnHere(-30);
			}
	
			Pose currentPose 	= this.pg.getPosition();
			int distance 		= (int)currentPose.distanceTo(this.pallet);
			radarDistance 		= this.radar.getRadarDistance();
			
			this.ia.setAllowInterrupt(true);
			
			if(this.radar.checkSomething()){
				this.ia.goForward(distance);
				if(!tryGrab()){
					failGrabHandler();
				}
			}
			
			this.ia.setAllowInterrupt(false);
		}
		this.ia.setResearchMode(false);
		updateStatus();
	}

	protected void failGrabHandler() {
		this.ia.goBackward(200);
		this.ia.turnHere(15);
		this.ia.goForward(225);
		
		if(!tryGrab()){
			this.ia.goBackward(225);
			this.ia.turnHere(-30);
			this.ia.goForward(250);
			tryGrab();
		}
	}
}
