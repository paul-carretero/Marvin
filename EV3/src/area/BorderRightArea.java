package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Représente des Area en bordure droite du Terrain
 */
public class BorderRightArea extends Area{
	
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
	public BorderRightArea(final int id, final int maxY , final Color maxYColor , final int minY , final Color minYColor ){
		super(id);
		
		this.maxY = maxY;
		this.minY = minY;
		
		this.maxYColor = maxYColor;
		this.minYColor = minYColor;
	}

	@Override
	public boolean getConsistency(final Pose p) {
		return (p.getX() > (Main.X_RED_LINE - MARGE_ERREUR) && 
				p.getY() < (this.maxY + MARGE_ERREUR) && p.getY() > (this.minY + MARGE_ERREUR));
	}

	@Override
	public Area colorChange(final Color color, final float h) {
		switch (this.id) {
			case 4:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(0);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(7);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(h) ){
					return Main.getArea(3);
				}
				break;
			case 7:
				if(color == Color.BLACK && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(9);
				}
				if(color == Color.BLUE && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(4);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(h) ){
					return Main.getArea(6);
				}
				break;
			case 9:
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(13);
				}
				if(color == Color.BLACK && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(7);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(h) ){
					return Main.getArea(6);
				}
				break;
			case 13:
				if(color == Color.WHITE && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(14);
				}
				if(color == Color.GREEN && checkAmbiguousAngleHorizontal(h) ){
					return Main.getArea(9);
				}
				if(color == Color.RED && checkAmbiguousAngleVertical(h) ){
					return Main.getArea(12);
				}
				break;
			default:
				System.err.println("Unsupproted ID on BorderRight Area, ID = " + this.id);
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
			1500,
			2000,
			this.minY,
			this.maxY
		};
	}

}
