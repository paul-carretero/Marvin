package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

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
	public int getConsistency(Pose p) {
		if(p.getX() > (Main.X_RED_LINE - MARGE_ERREUR) && 
				p.getY() < (maxY + MARGE_ERREUR) && p.getY() > (minY + MARGE_ERREUR)){
			return 0;
		}
		else{
			return (int) Math.max(
					Math.max(p.getX() - Main.X_RED_LINE, minY - p.getY()),
					p.getY() - maxY);
		}
	}

	@Override
	public Area colorChange(int color, Pose p) {
		switch (ID) {
			case 4:
				if(color == Main.COLOR_WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(0);
				}
				if(color == Main.COLOR_BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(7);
				}
				if(color == Main.COLOR_RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(3);
				}
				break;
			case 7:
				if(color == Main.COLOR_BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(9);
				}
				if(color == Main.COLOR_BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(4);
				}
				if(color == Main.COLOR_RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 9:
				if(color == Main.COLOR_GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(13);
				}
				if(color == Main.COLOR_BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(7);
				}
				if(color == Main.COLOR_RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 13:
				if(color == Main.COLOR_WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(14);
				}
				if(color == Main.COLOR_GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(9);
				}
				if(color == Main.COLOR_RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(12);
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
