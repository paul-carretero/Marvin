package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

public class BorderLeftArea extends Area {
	
	protected int maxY;
	protected Color maxYColor;
	
	protected int minY;
	protected Color minYColor;

	public BorderLeftArea(int id, int maxY , Color maxYColor , int minY , Color minYColor ){
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
	public Area colorChange(Color color, Pose p) {
		switch (ID) {
			case 1:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(0);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(5);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(2);
				}
				break;
			case 5:
				if(color == Color.BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(8);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(1);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 8:
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(10);
				}
				if(color == Color.BLACK && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(5);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(6);
				}
				break;
			case 10:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(14);
				}
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(8);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(11);
				}
				break;
		}
		if(color == Color.GREY){
			return this;
		}
		else{
			return Main.getArea(15); // default Area
		}
	}

}
