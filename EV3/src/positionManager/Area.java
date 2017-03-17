package positionManager;

import shared.Point;

public class Area {
	private int maxX;
	private int maxXColor;
	
	private int maxY;
	private int maxYColor;
	
	private int minX;
	private int minXColor;
	
	private int minY;
	private int minYColor;
	
	private char name;
	
	private static final int MARGE_ERREUR = 3;
	
	public Area(char c, int maxX , int maxXColor , int minX , int minXColor , int maxY , int maxYColor , int minY , int minYColor ){
		this.name = c;
		
		this.maxX = maxX;
		this.maxY = maxY;
		this.minX = minX;
		this.minY = minY;
		
		this.maxXColor = maxXColor;
		this.maxYColor = maxYColor;
		this.minXColor = minXColor;
		this.minYColor = minYColor;
	}
	
	public boolean checkConsistency(Point p){
		return (p.x() <= maxX + MARGE_ERREUR) && (p.x() >= minX - MARGE_ERREUR) && (p.y() <= maxY + MARGE_ERREUR) && (p.y() <= minY - MARGE_ERREUR);
	}
	
	
}
