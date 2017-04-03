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
	public boolean getConsistency(Pose p) {
		return (p.getX() < (Main.X_YELLOW_LINE + MARGE_ERREUR) && 
				p.getY() < (this.maxY + MARGE_ERREUR) && p.getY() > (this.minY + MARGE_ERREUR));
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		switch (this.id) {
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
		return Main.getArea(15); // default Area
	}

	@Override
	public float[] getBorder() {
		return new float[]{
			0,
			500,
			this.minY,
			this.maxY
		};
	}

}
