package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Représente une Area interne (entouré d'autre area).
 * Les ID possible sont 2,3,11 et 12
 */
public class InnerArea extends Area {
	/**
	 * borne X maximum
	 */
	protected final int maxX;
	/**
	 * Couleur de la ligne X de la borne maximum
	 */
	protected final Color maxXColor;
	
	/**
	 * borne Y maximum
	 */
	protected final int maxY;
	/**
	 * Couleur de la ligne Y de la borne maximum
	 */
	protected final Color maxYColor;
	
	/**
	 * borne X minimum
	 */
	protected final int minX;
	/**
	 * Couleur de la ligne X de la borne minimum 
	 */
	protected final Color minXColor;
	
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
	 * @param maxX borne X maximum
	 * @param maxXColor Couleur de la ligne X de la borne maximum
	 * @param minX borne X minimum
	 * @param minXColor Couleur de la ligne X de la borne minimum 
	 * @param maxY borne Y maximum
	 * @param maxYColor Couleur de la ligne Y de la borne maximum
	 * @param minY borne Y minimum
	 * @param minYColor Couleur de la ligne Y de la borne minimum 
	 */
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
	public boolean getConsistency(Pose p){
		return (p.getX() > (this.minX - MARGE_ERREUR) && p.getX() < (this.maxX + MARGE_ERREUR) && 
				p.getY() < (this.maxY + MARGE_ERREUR) && p.getY() > (this.minY + MARGE_ERREUR));
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		switch (this.id) {
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
		default:
			System.err.println("Unsupproted ID on InnerArea, ID = " + this.id);
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
			this.minX,
			this.maxX,
			this.minY,
			this.maxY
		};
	}
}
