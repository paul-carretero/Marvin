package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Point;

public class InnerArea extends Area {
	protected int maxX;
	protected int maxXColor;
	
	protected int maxY;
	protected int maxYColor;
	
	protected int minX;
	protected int minXColor;
	
	protected int minY;
	protected int minYColor;
	
	
	public InnerArea(int id, int maxX , int maxXColor , int minX , int minXColor , int maxY , int maxYColor , int minY , int minYColor ){
		super(id);
		
		this.maxX = maxX;
		this.maxY = maxY;
		this.minX = minX;
		this.minY = minY;
		
		this.maxXColor = maxXColor;
		this.maxYColor = maxYColor;
		this.minXColor = minXColor;
		this.minYColor = minYColor;
	}
	
	@Override
	public int getConsistency(Point p){
		return 100;
	}

	@Override
	public Area colorChange(int Color, Pose p) {
		return Main.getArea(15); // default Area
	}
}
