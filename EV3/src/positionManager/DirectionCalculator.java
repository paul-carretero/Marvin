package positionManager;

import aiPlanner.Main;
import interfaces.ItemGiver;
import interfaces.PoseGiver;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import shared.Item;

public class DirectionCalculator {
	
	private Point 		startPoint;
	private ItemGiver 	eom;
	private PoseGiver	pg;
	private static final int NO_ANGLE_FOUND	= 9999;
	private static final int MIN_DISTANCE	= 200;
	private static final int FIABLE_DIST	= 700;
	
	public DirectionCalculator(PositionCalculator pg){
		this.startPoint	= null;
		this.pg			= pg;
		
		Main.printf("[DIRECTION CALCULATOR]  : Initialized");
	}
	
	public void addEom(ItemGiver eom){
		this.eom = eom;
	}
	
	private float getAngle(Point p){
		if(p != null){
			if(this.startPoint.distance(p) > MIN_DISTANCE ){
				Main.printf("angle calculé = " + this.startPoint.angleTo(p));
				return this.startPoint.angleTo(p);
			}
		}
		return NO_ANGLE_FOUND;
	}
	
	private void updateAngle(Pose p){
		if(p != null){
			float calcAngle = getAngle(this.eom.getMarvinPosition().toLejosPoint());
			if(calcAngle != NO_ANGLE_FOUND){
				if(p.distanceTo(this.startPoint) < FIABLE_DIST ){
					p.setHeading((float) ((p.getHeading() * 0.4) + (calcAngle * 0.6)));
				}
				else{
					p.setHeading((float) ((p.getHeading() * 0.2) + (calcAngle * 0.8)));
				}
			}
		}
	}
	
	public void reset(){
		
		// on tente de mettre à jour l'angle si possible avant de reset
		
		if(this.startPoint != null){
			Pose myPose = this.pg.getPosition();
			updateAngle(myPose);
			this.pg.setPose(myPose);
		}
		
		// reset
		
		this.startPoint = null;
	}
	
	
	/*
	 * always forward
	 */
	public void startLine(){
		if(this.eom != null){
			Item eomStart = this.eom.getMarvinPosition();
			if(eomStart != null){
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
