package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Représente des Area en bordure gauche du Terrain
 */
public class BorderLeftArea extends Area {
	
	/**
	 * borne Y maximum
	 */
	protected final int maxY;
	
	/**
	 * Couleur de la ligne Y de la borne maximum
	 */
	protected final Color maxYColor;
	
	/**
	 * borne Y minimum
	 */
	protected final int minY;
	
	/**
	 * Couleur de la ligne Y de la borne minimum 
	 */
	protected final Color minYColor;

	/**
	 * @param id ID de l'area
	 * @param maxY borne Y maximum
	 * @param maxYColor Couleur de la ligne Y de la borne maximum
	 * @param minY borne Y minimum
	 * @param minYColor Couleur de la ligne Y de la borne minimum 
	 */
	public BorderLeftArea(final int id, final int maxY , final Color maxYColor , final int minY , final Color minYColor ){
		super(id);

		this.maxY = maxY;
		this.minY = minY;
		
		this.maxYColor = maxYColor;
		this.minYColor = minYColor;
	}

	@Override
	public boolean getConsistency(final Pose p) {
		return (p.getX() < (Main.X_YELLOW_LINE + MARGE_ERREUR) && 
				p.getY() < (this.maxY + MARGE_ERREUR) && p.getY() > (this.minY + MARGE_ERREUR));
	}

	@Override
	public Area colorChange(final Color color, final Pose p) {
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
			default:
				System.err.println("Unsupproted ID on BorderLeft Area, ID = " + this.id);
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
