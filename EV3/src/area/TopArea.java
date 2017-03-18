package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;

public class TopArea extends Area{

	private final int yBottom = 270;
	
	public TopArea(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getConsistency(Pose p) {
		if(p.getY() > (yBottom - MARGE_ERREUR)){
			return 0;
		}
		else{
			return (int) (yBottom - p.getY());
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
					return Main.getArea(10);
				}
				else if(p.getX() > Main.X_YELLOW_LINE && p.getX() < Main.X_BLACK_LINE){
					return Main.getArea(11);
				}
				else if(p.getX() > Main.X_BLACK_LINE && p.getX() < Main.X_RED_LINE){
					return Main.getArea(12);
				}
				else if(p.getX() > Main.X_RED_LINE){
					return Main.getArea(13);
				}
			}
		}
		return Main.getArea(15); // default Area
	}

}
