package shared;

import aiPlanner.Main;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Classe representant un point ayant des coordonnées entière dans un repère cartésien
 * Permet des opérations simple sur les points
 */
public class IntPoint {
	
	/**
	 * coordonné du point sur l'axe x
	 */
	
	protected int x;
	/**
	 * coordonné du point sur l'axe y
	 */
	protected int y;
	
	/**
	 * @param x coordonné du point sur l'axe x
	 * @param y coordonné du point sur l'axe y
	 */
	public IntPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param p un Point utilisé par la bibliothèque LeJos
	 * @see Point
	 */
	public IntPoint(final Point p) {
		this.x = (int) p.getX();
		this.y = (int) p.getY();
	}


	/**
	 * @param pose une pose de robot
	 * @see Pose
	 */
	public IntPoint(final Pose pose) {
		this.x = (int) pose.getX();
		this.y = (int) pose.getY();
	}


	/**
	 * @return retourne la coordonné x
	 */
	public int x(){
		return this.x;
	}
	
	/**
	 * @return retourne la coordonné y
	 */
	public int y(){
		return this.y;
	}
	
	/**
	 * @param x une nouvelle coordonné x
	 * @return met à jour la coordonné x
	 */
	public int setX(final int x){
		return this.x = x;
	}
	
	/**
	 * @param y une nouvelle coordonné y
	 * @return met à jour la coordonné y
	 */
	public int setY(final int y){
		return this.y = y;
	}
	
	@Override
	public boolean equals(final Object  o){
		if (!(o instanceof IntPoint)) {
	        return false;
	    }
		IntPoint p = (IntPoint)o;
		return (y() == p.y() && x() == p.x());
	}
	
	@Override
	public int hashCode() {
		return (this.x()) + (10000 * (this.y())); // parfait si x et y < 10000...
	}
	
	/**
	 * Met à jour les coordonnés du point
	 * @param x coordonné du point sur l'axe x
	 * @param y coordonné du point sur l'axe y
	 */
	public void update(final int x, final int y){
		this.x = x;
		this.y = y;
	}

	/**
	 * @param p2 un IntPoint non null
	 * @return la distance entre ce point et p2 (entier positif)
	 */
	public int getDistance(final IntPoint p2){
		return (int) Math.round(Math.sqrt((this.x-p2.x())*(this.x-p2.x()) + (this.y-p2.y())*(this.y-p2.y())));
	}
	
	/**
	 * @return une nouvelle instance d'un objet point de la bibliothèque LeJos
	 * @see Point
	 */
	public Point toLejosPoint(){
		return new Point(this.x,this.y);
	}
	
	/**
	 * @param p2 une autre IntPoint
	 * @return le vecteur en partance de ce point vers P2
	 */
	public IntPoint computeVector(final IntPoint p2){
		return new IntPoint((p2.x() - this.x()) , (p2.y() - this.y()));
	}
	
	/**
	 * calcul le point d'intercection avec la droite à 20 cm de la ligne d'objectif
	 * @param vecteur un IntPoint représentant un vecteur
	 * @return retourne le point d'interction entre la droite partant de ce point avec le vecteur et la ligne de défense
	 */
	public IntPoint getIntersection(final IntPoint vecteur){
		try{
			int coeff = Math.max(Main.Y_DEFEND_WHITE - this.y(),this.y() - Main.Y_DEFEND_WHITE) / vecteur.y();
			
			IntPoint res = new IntPoint(vecteur.x() * coeff + this.x(), vecteur.y() * coeff + this.y());
			
			if(res.y() < 1500){
				res.setY(Main.Y_DEFEND_WHITE + 200);
			}
			else{
				res.setY(Main.Y_DEFEND_WHITE - 200);
			}
			
			return res;
		}
		catch (Exception e) {
			// division par 0
			return null;
		}
	}
	
	@Override
	public String toString(){
		return "[X = " + this.x + " Y = " + this.y + "]";
	}
}
