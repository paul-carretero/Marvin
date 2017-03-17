package shared;

import aiPlanner.Main;

public class TimedPoint extends Point{
	private int createdTime;
	private int referenceTime;
	private Point referencePoint;
	private boolean average; // true si le point n'est pas précis


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
	
	public int getMovedDistance(){
		return (int) Math.round(this.getDistance(referencePoint));
	}
	
	public double getAvgSpeed(){
		return this.getDistance(referencePoint)/(referenceTime - createdTime);
	}
	
	public TimedPoint(int x, int y, int currentTime) {
		super(x, y);
		createdTime = currentTime;
		referenceTime = currentTime;
	}
	
	public void updateTimeStamp(int currentTime) {
		referenceTime = currentTime;
	}
	
	public TimedPoint(int x, int y) {
		super(x, y);
		createdTime = Main.TIMER.getElapsedMs();
		referenceTime = Main.TIMER.getElapsedMs();
	}
	
	public TimedPoint(){
		super(0, 0);
		createdTime = 0;
	}

	public boolean isAverage() {
		return average;
	}

	public void setAverage(boolean average) {
		this.average = average;
	}

	public int getReferenceTime() {
		return referenceTime;
	}

}
