package shared;

import aiPlanner.Main;

public class TimedPoint extends IntPoint{
	private int createdTime;
	private int referenceTime;

	public TimedPoint(int x, int y, int currentTime) {
		super(x, y);
		createdTime = currentTime;
		referenceTime = currentTime;
	}
	
	public TimedPoint(int x, int y) {
		super(x, y);
		createdTime = Main.TIMER.getElapsedMs();
		referenceTime = Main.TIMER.getElapsedMs();
	}	
	
	public void update(int x, int y, int currentTime){
		this.x = x;
		this.y = y;
		this.referenceTime = currentTime;
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
		this.referenceTime = Main.TIMER.getElapsedMs();
	}
	
	public void silentUpdate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void updateTimeStamp(int currentTime) {
		referenceTime = currentTime;
	}

	public int getReferenceTime() {
		return referenceTime;
	}
	
	public int getLifeTime(){
		return referenceTime - createdTime;
	}

}
