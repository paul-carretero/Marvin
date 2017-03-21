package positionManager;

import aiPlanner.Main;
import interfaces.ModeListener;
import interfaces.MoveListener;
import interfaces.PoseGiver;
import interfaces.SignalListener;
import shared.Mode;
import shared.SignalType;
import shared.TimedPose;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class PositionCalculator extends Thread implements ModeListener, MoveListener, PoseGiver, SignalListener {

	private Pose 					lastCalculatedPosition;
	private int 					refreshRate = 250;
	private volatile Mode			mode;
	private VisionSensor 			radar;
	private OdometryPoseProvider 	odometryPoseProvider;
	
	public PositionCalculator(){
		this.lastCalculatedPosition	= new TimedPose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL);
		this.mode 					= Mode.ACTIVE;
		this.radar 					= new VisionSensor();
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	public void run() {
		Main.printf("[POSITION CALCULATOR]   : Started");
		while(!isInterrupted() && mode != Mode.END){
			//Main.printf("[POSITION CALCULATOR]   : " + lastCalculatedPosition.toString());
			//Main.printf("[POSITION CALCULATOR]   : Radar : " + radar.getNearItemDistance());
			updatePose();
			syncWait();
		}
		Main.printf("[POSITION CALCULATOR]   : Finished");
		
	}
	
	private void updatePose() {
		Pose odometriquePose = odometryPoseProvider.getPose();
		//TODO adjust with radar
		//TODO adust with Map
		//TODO adjust with Area
		lastCalculatedPosition = odometriquePose;
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

	public Pose getPosition() {
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

	public void sendFixX(int x) {
		Pose tempPose = odometryPoseProvider.getPose();
		tempPose.setLocation(x, tempPose.getY());
		odometryPoseProvider.setPose(tempPose);
	}

	public void sendFixY(int y) {
		Pose tempPose = odometryPoseProvider.getPose();
		tempPose.setLocation(tempPose.getX(), y);
		odometryPoseProvider.setPose(tempPose);
	}

	public int getRadarDistance() {
		return radar.getNearItemDistance();
	}
}
