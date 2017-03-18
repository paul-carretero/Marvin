package shared;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

public class TimedPose extends Pose{
	
	protected int timer;

	public TimedPose(float f, float g, float h) {
		super(f,g,h);
		timer = Main.TIMER.getElapsedMs();
	}
	
	public TimedPose(float xInitial, float yInitial, float headingInitial, int time) {
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
