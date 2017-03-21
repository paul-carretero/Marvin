package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

public abstract class Area {

	protected int ID;
	protected final static int AMBIGUOUS_ANGLE = 10;
	protected static final int MARGE_ERREUR = 30; // en mm
	
	public Area(int id){
		this.ID = id;
	}
	
	public static Area getAreaWithPosition(Pose p){
		if(p.getY() > (Main.Y_TOP_WHITE)){
			return Main.getArea(0);
		}
		else if(p.getY() < (Main.Y_BOTTOM_WHITE)){
			return Main.getArea(14);
		}
		else if(p.getY() < (Main.Y_GREEN_LINE) && p.getY() > (Main.Y_BOTTOM_WHITE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(10);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(11);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(12);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(13);
			}
		}
		else if(p.getY() < (Main.Y_BLACK_LINE) && p.getY() > (Main.Y_GREEN_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(8);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(9);
			}
		}
		else if(p.getY() < (Main.Y_BLUE_LINE) && p.getY() > (Main.Y_BLACK_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(5);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(7);
			}
		}
		else if(p.getY() < (Main.Y_TOP_WHITE) && p.getY() > (Main.Y_BLUE_LINE)){
			if(p.getX() < (Main.X_YELLOW_LINE)){
				return Main.getArea(1);
			}
			else if(p.getX() > (Main.X_YELLOW_LINE) && p.getX() < (Main.X_BLACK_LINE)){
				return Main.getArea(2);
			}
			else if(p.getX() > (Main.X_BLACK_LINE) && p.getX() < (Main.X_RED_LINE)){
				return Main.getArea(3);
			}
			else if(p.getX() > (Main.X_RED_LINE)){
				return Main.getArea(4);
			}
		}
		return Main.getArea(15);
	}
	
	public String toString(){
		return "A"+ID;
	}
	
	public abstract int getConsistency(Pose p);
	
	public abstract Area colorChange(int color, Pose p);
	
	public static boolean checkAmbiguousAngleVertical(Pose p){
		return Math.abs(p.getHeading()) > (90 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (90 - AMBIGUOUS_ANGLE);
	}
	
	public static boolean checkAmbiguousAngleHorizontal(Pose p){
		return Math.abs(p.getHeading()) > (0 + AMBIGUOUS_ANGLE) || 
				Math.abs(p.getHeading()) < (180 - AMBIGUOUS_ANGLE);
	}
}
