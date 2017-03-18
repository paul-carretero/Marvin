package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Point;

public abstract class Area {

	protected int ID;
	protected final static int AMBIGUOUS_ANGLE = 10;
	
	public Area(int id){
		this.ID = id;
	}
	
	public static Area getAreaWithPosition(Point p){
		if(p.y() < (Main.Y_BOTTOM_WHITE)){
			return Main.getArea(0);
		}
		else if(p.y() > (Main.Y_TOP_WHITE)){
			return Main.getArea(14);
		}
		else if(p.y() > (Main.Y_GREEN_LINE) && p.y() < (Main.Y_TOP_WHITE)){
			if(p.x() < (Main.X_YELLOW_LINE)){
				return Main.getArea(10);
			}
			else if(p.x() > (Main.X_YELLOW_LINE) && p.x() < (Main.X_BLACK_LINE)){
				return Main.getArea(11);
			}
			else if(p.x() > (Main.X_BLACK_LINE) && p.x() < (Main.X_RED_LINE)){
				return Main.getArea(12);
			}
			else if(p.x() > (Main.X_RED_LINE)){
				return Main.getArea(13);
			}
		}
		else if(p.y() > (Main.Y_BLACK_LINE) && p.y() < (Main.Y_GREEN_LINE)){
			if(p.x() < (Main.X_YELLOW_LINE)){
				return Main.getArea(8);
			}
			else if(p.x() > (Main.X_YELLOW_LINE) && p.x() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.x() > (Main.X_BLACK_LINE) && p.x() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.x() > (Main.X_RED_LINE)){
				return Main.getArea(9);
			}
		}
		else if(p.y() > (Main.Y_BLUE_LINE) && p.y() < (Main.Y_BLACK_LINE)){
			if(p.x() < (Main.X_YELLOW_LINE)){
				return Main.getArea(5);
			}
			else if(p.x() > (Main.X_YELLOW_LINE) && p.x() < (Main.X_BLACK_LINE)){
				return Main.getArea(6);
			}
			else if(p.x() > (Main.X_BLACK_LINE) && p.x() < (Main.X_RED_LINE)){
				return Main.getArea(6);
			}
			else if(p.x() > (Main.X_RED_LINE)){
				return Main.getArea(7);
			}
		}
		else if(p.y() > (Main.Y_BOTTOM_WHITE) && p.y() < (Main.Y_BLUE_LINE)){
			if(p.x() < (Main.X_YELLOW_LINE)){
				return Main.getArea(1);
			}
			else if(p.x() > (Main.X_YELLOW_LINE) && p.x() < (Main.X_BLACK_LINE)){
				return Main.getArea(2);
			}
			else if(p.x() > (Main.X_BLACK_LINE) && p.x() < (Main.X_RED_LINE)){
				return Main.getArea(3);
			}
			else if(p.x() > (Main.X_RED_LINE)){
				return Main.getArea(4);
			}
		}
		return Main.getArea(15);
	}
	
	protected static final int MARGE_ERREUR = 3;
	
	public abstract int getConsistency(Point p);
	
	public abstract Area colorChange(int Color, Pose p);
}
