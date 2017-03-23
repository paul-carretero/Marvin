package positionManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Item;

public class DirectionCalculator {
	
	private Point startPoint 	= null;
	private boolean forward		= true;
	private ItemGiver eom		= null;
	
	public DirectionCalculator(){
		this.startPoint	= null;
		this.forward	= true;
		Main.printf("[DIRECTION CALCULATOR]  : Initialized");
	}
	
	public void addEom(ItemGiver eom){
		this.eom = eom;
	}
	
	protected float getAngle(Point p){
		if(startPoint != null && p != null){
			// pas sur si la distance est de moins de 10cm, ajustable éventuellement
			if(startPoint.distance(p) > 100 ){
				float realAngle = startPoint.angleTo(p);
				if(forward){
					return realAngle;
				}
				else{
					if(realAngle > 0){
						return realAngle - 180;
					}
					else{
						return realAngle + 180;
					}
				}
			}
		}
		return 9999;
	}
	
	/*
	 * plus la différence est importante plus on corrige vite
	 * TODO vérifier que angle est bien toujours entre -180 et 180...
	 */
	public void updateAngle(Pose p){
		float calcAngle = getAngle(p.getLocation());
		if(calcAngle != 9999){
			if(Math.abs(calcAngle - p.getHeading()) < 5){
				p.setHeading((float) ((p.getHeading() * 0.9) + (calcAngle * 0.1)));
			}
			else if(Math.abs(calcAngle - p.getHeading()) < 20){
				p.setHeading((float) ((p.getHeading() * 0.7) + (calcAngle * 0.3)));
			}
			else{
				p.setHeading((float) ((p.getHeading() * 0.5) + (calcAngle * 0.5)));
			}
		}
	}
	
	public void reset(){
		startPoint = null;
	}
	
	public void startLine(boolean forward){
		if(eom != null){
			Item eomStart = eom.getMarvinPosition();
			if(eomStart != null){
				this.forward 	= forward;
				this.startPoint	= eomStart.toLejosPoint();
			}
			else{
				Main.printf("[ERREUR]                : Impossible d'initialiser le DirectionCalculator (getMarvinPosition null)");
			}
		}
		else{
			Main.printf("[ERREUR]                : Impossible d'initialiser le DirectionCalculator (eom null)");
		}
	}

}
