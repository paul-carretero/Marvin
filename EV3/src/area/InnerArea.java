package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

public class InnerArea extends Area {
	protected int maxX;
	protected Color maxXColor;
	
	protected int maxY;
	protected Color maxYColor;
	
	protected int minX;
	protected Color minXColor;
	
	protected int minY;
	protected Color minYColor;
	
	
	public InnerArea(int id, int maxX , Color maxXColor , int minX , Color minXColor , int maxY , Color maxYColor , int minY , Color minYColor ){
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
	public int getConsistency(Pose p){
		if(p.getX() > (minX - MARGE_ERREUR) && p.getX() < (maxX + MARGE_ERREUR) && 
				p.getY() < (maxY + MARGE_ERREUR) && p.getY() > (minY + MARGE_ERREUR)){
			return 0;
		}
		else{
			return (int) Math.max(
					Math.max(p.getY() - maxY, minY - p.getY()),
					Math.max(p.getX() - maxX, minX - p.getX()));
		}
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		switch (ID) {
			case 2:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(0);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(6);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(1);
				}
				if(color == Color.BLACK && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(3);
				}
				break;
			case 3:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(0);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(6);
				}
				if(color == Color.BLACK && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(2);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(4);
				}
				break;
			case 11:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(14);
				}
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(6);
				}
				if(color == Color.YELLOW && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(10);
				}
				if(color == Color.BLACK && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(12);
				}
				break;
			case 12:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(14);
				}
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(p) ){
					return Main.getArea(6);
				}
				if(color == Color.BLACK && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(11);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(p) ){
					return Main.getArea(13);
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
