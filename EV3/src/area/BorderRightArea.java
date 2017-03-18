package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Point;

public class BorderRightArea extends Area{
	
	protected int maxY;
	protected int maxYColor;
	
	protected int minY;
	protected int minYColor;

	public BorderRightArea(int id, int maxY , int maxYColor , int minY , int minYColor ){
		super(id);
		
		this.maxY = maxY;
		this.minY = minY;
		
		this.maxYColor = maxYColor;
		this.minYColor = minYColor;
	}

	@Override
	public int getConsistency(Point p) {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public Area colorChange(int Color, Pose p) {
		return Main.getArea(15); // default Area
	}

}
