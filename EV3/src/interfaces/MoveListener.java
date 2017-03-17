package interfaces;

import shared.TimedPose;

public interface MoveListener {
	public void movementForward(TimedPose start, int speed, int distance);
	public void movementBackward(TimedPose start, int speed, int distance);
	public void turnSmooth(int angle, int startTime);
	public void turnHere(int angle, int startTime);
}
