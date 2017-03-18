package shared;

public class Point {
	protected int x;
	protected int y;
	
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
		if (!(o instanceof Point)) {
	        return false;
	    } else {
	    	Point p = (Point)o;
	    	return (y() == p.y() && x() == p.x());
	    }
	}
	
	@Override
	public int hashCode() {
		return (this.x()) + (1000 * (this.y())); // parfait si x et y < 1000...
	}
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getDistance(Point p2){
		return (int) Math.round(Math.sqrt((x-p2.x)^2 + (y-p2.y)^2));
	}
}
