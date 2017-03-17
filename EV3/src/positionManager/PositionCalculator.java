package positionManager;

import aiPlanner.Main;
import interfaces.ModeListener;
import interfaces.MoveListener;
import interfaces.PositionGiver;
import interfaces.SignalListener;
import shared.Mode;
import shared.SignalType;
import shared.TimedPose;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class PositionCalculator extends Thread implements ModeListener, MoveListener, PositionGiver, SignalListener {

	private TimedPose 				latestXCheck;
	private TimedPose 				latestYCheck;
	private TimedPose 				lastCalculatedPosition;
	private int 					refreshRate = 300;
	private volatile Mode			mode;
	private VisionSensor 			radar;
	private OdometryPoseProvider 	odometryPoseProvider;
	
	
	public void run() {
		Main.printf("[POSITION CALCULATOR]   : Started");
		while(!isInterrupted()){
			Main.printf("[POSITION CALCULATOR]   : " + odometryPoseProvider.getPose().toString());
			syncWait();
		}
		Main.printf("[POSITION CALCULATOR]   : Finished");
		
	}
	
	public PositionCalculator(){
		this.latestXCheck 			= new TimedPose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.latestYCheck 			= new TimedPose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.lastCalculatedPosition	= new TimedPose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.mode 					= Mode.ACTIVE;
		this.radar 					= new VisionSensor();
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	public void addOdometryPoseProvider(MoveProvider mp){
		odometryPoseProvider = new OdometryPoseProvider(mp);
		updateOdometry(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
	}
	
	public void updateOdometry(Pose p){
		odometryPoseProvider.setPose(p);
	}
	
	public void syncWait(){
		synchronized (this) {
			try {
				this.wait(refreshRate);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void setMode(Mode m) {
		this.mode = m;
	}

	public TimedPose getPosition() {
		return lastCalculatedPosition;
	}

	public void movementForward(TimedPose start, int speed, int distance) {
		// TODO Auto-generated method stub
		
	}

	public void movementBackward(TimedPose start, int speed, int distance) {
		// TODO Auto-generated method stub
		
	}

	public void turnSmooth(int angle, int startTime) {
		// TODO Auto-generated method stub
		
	}
	
	public void turnHere(int angle, int startTime) {
		// TODO Auto-generated method stub
		
	}

	public void signal(SignalType e) {
		// TODO Auto-generated method stub
		
	}
}
