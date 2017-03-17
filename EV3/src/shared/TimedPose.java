package shared;

import lejos.robotics.navigation.Pose;

public class TimedPose extends Pose{
	
	protected int timer;

	public TimedPose(int xInitial, int yInitial, int headingInitial) {
		super(xInitial,yInitial,headingInitial);
		timer = 0;
	}
	
	public TimedPose(int xInitial, int yInitial, int headingInitial, int time) {
		super(xInitial,yInitial,headingInitial);
		this.timer = time;
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}
	
	public TimedPoint toTimedPoint(){
		return new TimedPoint((int)this._location.x,(int)this._location.y,timer);
	}

}
