package shared;

import aiPlanner.Main;
import lejos.robotics.geometry.Point;

public class IntPoint {
	protected int x;
	protected int y;
	
	public IntPoint(float x, float y) {
		this.x = (int) x;
		this.y = (int) y;
	}
	
	
	public IntPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int x(){
		return x;
	}
	
	public int y(){
		return y;
	}
	
	public int setX(int x){
		return this.x = x;
	}
	
	public int setY(int y){
		return this.y = y;
	}
	
	@Override
	public boolean equals(Object  o){
		if (!(o instanceof IntPoint)) {
	        return false;
	    } else {
	    	IntPoint p = (IntPoint)o;
	    	return (y() == p.y() && x() == p.x());
	    }
	}
	
	@Override
	public int hashCode() {
		return (this.x()) + (10000 * (this.y())); // parfait si x et y < 10000...
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getDistance(IntPoint p2){
		return (int) Math.round(Math.sqrt((x-p2.x())*(x-p2.x()) + (y-p2.y())*(y-p2.y())));
	}
	
	public Point toLejosPoint(){
		return new Point(x,y);
	}
}
