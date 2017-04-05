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
	protected 		Point			palet;
	protected 		PoseGiver		pg;
	protected		ItemGiver		eom;
	protected		DistanceGiver 	radar;
	protected final static int		MARGE = 100;

	public GoalGrabPessimist(GoalFactory gf, Marvin ia, Point palet, PoseGiver pg, ItemGiver eom, DistanceGiver radar) {
		super(gf, ia);
		this.radar	= radar;
		this.eom 	= eom;
		this.palet	= palet;
		this.pg 	= pg;
	}

	protected void correctPosition(){
		Pose currentPose = this.pg.getPosition();

		int distance = (int)currentPose.distanceTo(this.palet);
		int angleCorrection = (int)currentPose.relativeBearing(this.palet);
		
		this.ia.turnHere(angleCorrection);
		
		if(distance < Main.RADAR_DEFAULT_RANGE + MARGE){
			this.ia.goBackward(Main.RADAR_DEFAULT_RANGE - distance);
		}
		else if(distance > Main.RADAR_DEFAULT_RANGE - MARGE){
			this.ia.goForward(distance - Main.RADAR_DEFAULT_RANGE);
		}
	}
	
	protected boolean tryGrab(){
		if(Main.PRESSION){
			Main.HAVE_PALET = true;
			this.ia.grab();
			return true;
		}
		return false;
	}
	
	protected void updateStatus(){
		this.gf.setLastGrab(Main.HAVE_PALET);
	}
	
	protected void grabWrapper(){
		Pose currentPose 	= this.pg.getPosition();
		int distance 		= (int)currentPose.distanceTo(this.palet);
		
		this.ia.setAllowInterrupt(true);
		
		if(this.radar.checkSomething()){
			this.ia.goForward(distance);
			
			if(!tryGrab()){
				failGrabHandler();
			}
		}
		
		this.ia.setAllowInterrupt(false);
	}
	
	private void setBestAngle(){
		int radarDistance 	= Main.RADAR_OUT_OF_BOUND;
		int previousRadar 	= Main.RADAR_OUT_OF_BOUND;
		
		this.ia.turnHere(-25);
		radarDistance = this.radar.getRadarDistance();
		previousRadar = radarDistance;
		this.ia.turnHere(25);
		radarDistance = this.radar.getRadarDistance();
		if (radarDistance > previousRadar){
			this.ia.turnHere(-25);
		}
		else{
			previousRadar = radarDistance;
			this.ia.turnHere(25);
			radarDistance = this.radar.getRadarDistance();
			if (radarDistance > previousRadar){
				this.ia.turnHere(-25);
			}
		}
	}
	
	@Override
	public void start() {
		
		if(this.eom.checkpalet(new IntPoint(this.palet))){
			
			correctPosition();
			setBestAngle();
			grabWrapper();
			
		}
		
		updateStatus();
	}

	protected void failGrabHandler() {
		this.ia.goBackward(200);
		this.ia.turnHere(13);
		this.ia.goForward(225);
		
		if(!tryGrab()){
			this.ia.goBackward(225);
			this.ia.turnHere(-26);
			this.ia.goForward(250);
			tryGrab();
		}
	}
	
	@Override
	public GoalType getName(){
		return this.NAME;
	}

	@Override
	protected boolean checkPreConditions() {
		return Main.HAND_OPEN && !Main.HAVE_PALET;
	}
}
