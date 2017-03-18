package interfaces;

import shared.TimedPose;

public interface PoseGiver {
	public TimedPose getPosition();
	public void sendFixX(int x);
	public void sendFixY(int y);
}
