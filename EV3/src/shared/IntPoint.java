package shared;

import aiPlanner.Main;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Classe representant un point ayant des coordonn�es enti�re dans un rep�re cart�sien
 * Permet des op�rations simple sur les points
 */
public class IntPoint {
	
	/**
	 * coordonn� du point sur l'axe x
	 */
	
	protected int x;
	/**
	 * coordonn� du point sur l'axe y
	 */
	protected int y;
	
	/**
	 * @param x coordonn� du point sur l'axe x
	 * @param y coordonn� du point sur l'axe y
	 */
	public IntPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param p un Point utilis� par la biblioth�que LeJos
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
	 * @return retourne la coordonn� x
	 */
	public int x(){
		return this.x;
	}
	
	/**
	 * @return retourne la coordonn� y
	 */
	public int y(){
		return this.y;
	}
	
	/**
	 * @param x une nouvelle coordonn� x
	 * @return met � jour la coordonn� x
	 */
	public int setX(final int x){
		return this.x = x;
	}
	
	/**
	 * @param y une nouvelle coordonn� y
	 * @return met � jour la coordonn� y
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
	 * Met � jour les coordonn�s du point
	 * @param x coordonn� du point sur l'axe x
	 * @param y coordonn� du point sur l'axe y
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
	 * @return une nouvelle instance d'un objet point de la biblioth�que LeJos
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
	 * calcul le point d'intercection avec la droite � 20 cm de la ligne d'objectif
	 * @param vecteur un IntPoint repr�sentant un vecteur
	 * @return retourne le point d'interction entre la droite partant de ce point avec le vecteur et la ligne de d�fense
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
