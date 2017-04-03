package shared;

import aiPlanner.Main;

public class TimedPoint extends IntPoint{
	private int createdTime;
	private int referenceTime;

	public TimedPoint(int x, int y, int currentTime) {
		super(x, y);
		this.createdTime	= currentTime;
		this.referenceTime	= currentTime;
	}
	
	public TimedPoint(int x, int y) {
		super(x, y);
		this.createdTime	= Main.TIMER.getElapsedMs();
		this.referenceTime	= Main.TIMER.getElapsedMs();
	}	
	
	public void update(int x, int y, int currentTime){
		this.x				= x;
		this.y				= y;
		this.referenceTime	= currentTime;
	}
	
	@Override
	public void update(int x, int y){
		this.x				= x;
		this.y				= y;
		this.referenceTime	= Main.TIMER.getElapsedMs();
	}
	
	public void silentUpdate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void updateTimeStamp(int currentTime) {
		this.referenceTime	= currentTime;
	}

	public int getReferenceTime() {
		return this.referenceTime;
	}
	
	public int getLifeTime(){
		return this.referenceTime - this.createdTime;
	}

}
