package positionManager;

import aiPlanner.Main;
import interfaces.ModeListener;
import interfaces.PoseGiver;
import shared.Mode;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;

public class PositionCalculator extends Thread implements ModeListener, PoseGiver {

	private int 					refreshRate = 400;
	private volatile Mode			mode;
	private VisionSensor 			radar;
	private OdometryPoseProvider 	odometryPoseProvider;
	private DirectionCalculator 	directionCalculator;
	
	public PositionCalculator(MoveProvider mp, DirectionCalculator directionCalculator){
		this.mode 					= Mode.ACTIVE;
		this.radar 					= new VisionSensor();
		this.odometryPoseProvider 	= new OdometryPoseProvider(mp);
		this.directionCalculator	= directionCalculator;
		
		odometryPoseProvider.setPose(new Pose(Main.X_INITIAL, Main.Y_INITIAL, Main.H_INITIAL));
		
		Main.printf("[POSITION CALCULATOR]   : Initialized");
	}
	
	public void run() {
		Main.printf("[POSITION CALCULATOR]   : Started");
		while(!isInterrupted() && mode != Mode.END){
			updatePose();
			
			//Main.printf("[POSITION CALCULATOR]   : " + lastCalculatedPosition.toString());
			//Main.printf("[POSITION CALCULATOR]   : Radar : " + radar.getNearItemDistance());
			
			syncWait();
		}
		Main.printf("[POSITION CALCULATOR]   : Finished");
		
	}
	
	private void updatePose() {						// define position / define lost
		Pose pose = odometryPoseProvider.getPose(); // 60%			   / 0%
		//TODO adjust with radar					// 10%			   / 20%
		//TODO adjust with Map						// 30%			   / 40%
		//TODO adjust with Area						// 0%			   / 40%
		//directionCalculator.updateAngle(pose);
		odometryPoseProvider.setPose(pose);
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
		return odometryPoseProvider.getPose();
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
