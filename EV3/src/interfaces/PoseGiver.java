package interfaces;

import lejos.robotics.navigation.Pose;

public interface PoseGiver {
	public Pose getPosition();
	public void sendFixX(int x);
	public void sendFixY(int y);
	public int getAreaId();
}
