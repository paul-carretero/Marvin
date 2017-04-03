package shared;

import aiPlanner.Main;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

public class IntPoint {
	protected int x;
	protected int y;
	
	public IntPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public IntPoint(Point p) {
		this.x = (int) p.getX();
		this.y = (int) p.getY();
	}


	public IntPoint(Pose pose) {
		this.x = (int) pose.getX();
		this.y = (int) pose.getY();
	}


	public int x(){
		return this.x;
	}
	
	public int y(){
		return this.y;
	}
	
	public int setX(int x){
		return this.x = x;
	}
	
	public int setY(int y){
		return this.y = y;
	}
	
	@Override
	public boolean equals(Object  o){
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
	
	public void update(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getDistance(IntPoint p2){
		return (int) Math.round(Math.sqrt((this.x-p2.x())*(this.x-p2.x()) + (this.y-p2.y())*(this.y-p2.y())));
	}
	
	public Point toLejosPoint(){
		return new Point(this.x,this.y);
	}
	
	/*
	 * @return le vecteur en partance de ce point vers P2
	 */
	public IntPoint computeVector(IntPoint p2){
		return new IntPoint((p2.x() - this.x()) , (p2.y() - this.y()));
	}
	
	/*
	 * retourne le point d'intercection avec la droite à 20 cm des buts
	 * assume que le intPoint est le vecteur de la droite
	 */
	public IntPoint getIntersection(IntPoint vecteur){
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
