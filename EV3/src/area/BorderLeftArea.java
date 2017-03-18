package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

public class BorderLeftArea extends Area {
	
	protected int maxY;
	protected int maxYColor;
	
	protected int minY;
	protected int minYColor;

	public BorderLeftArea(int id, int maxY , int maxYColor , int minY , int minYColor ){
		super(id);

		this.maxY = maxY;
		this.minY = minY;
		
		this.maxYColor = maxYColor;
		this.minYColor = minYColor;
	}

	@Override
	public int getConsistency(Pose p) {
		if(p.getX() < (Main.X_YELLOW_LINE + MARGE_ERREUR) && 
				p.getY() < (maxY + MARGE_ERREUR) && p.getY() > (minY + MARGE_ERREUR)){
			return 0;
		}
		else{
			return (int) Math.max(
					Math.max(Main.X_YELLOW_LINE - p.getX(), minY - p.getY()),
					p.getY() - maxY);
		}
	}

	@Override
	public Area colorChange(int color, Pose p) {
		switch (ID) {
			case 1:
				if(color == Main.COLOR_WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(0);
				}
				if(color == Main.COLOR_BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(5);
				}
				if(color == Main.COLOR_YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(2);
				}
				break;
			case 5:
				if(color == Main.COLOR_BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(8);
				}
				if(color == Main.COLOR_BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(1);
				}
				if(color == Main.COLOR_YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 8:
				if(color == Main.COLOR_GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(10);
				}
				if(color == Main.COLOR_BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(5);
				}
				if(color == Main.COLOR_YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 10:
				if(color == Main.COLOR_WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(14);
				}
				if(color == Main.COLOR_GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(8);
				}
				if(color == Main.COLOR_YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(11);
				}
				break;
		}
		if(color == Main.COLOR_GREY){
			return this;
		}
		else{
			return Main.getArea(15); // default Area
		}
	}

}
