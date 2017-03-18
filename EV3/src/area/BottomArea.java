package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

public class BottomArea extends Area{
	
	private final int yTop = 30;

	public BottomArea(int id) {
		super(id);
	}

	@Override
	public int getConsistency(Pose p) {
		if(p.getY() < (yTop + MARGE_ERREUR)){
			return 0;
		}
		else{
			return (int) (p.getY() - yTop);
		}
	}

	@Override
	public Area colorChange(int color, Pose p) {
		if(color == Main.COLOR_GREY){
			return this;
		}
		else if(color == Main.COLOR_WHITE){
			// on évite les cas limites
			if( checkAmbiguousAngleHorizontal(p) ){
				if(p.getX() < Main.X_YELLOW_LINE){
					return Main.getArea(1);
				}
				if(p.getX() > Main.X_YELLOW_LINE && p.getX() < Main.X_BLACK_LINE){
					return Main.getArea(2);
				}
				if(p.getX() > Main.X_BLACK_LINE && p.getX() < Main.X_RED_LINE){
					return Main.getArea(3);
				}
				if(p.getX() > Main.X_RED_LINE){
					return Main.getArea(4);
				}
			}
		}
		return Main.getArea(15); // default Area
	}

}
