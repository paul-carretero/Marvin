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
