package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

public class TopArea extends Area{
	
	public TopArea(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getConsistency(Pose p) {
		return (p.getY() > (Main.Y_TOP_WHITE - MARGE_ERREUR));
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		if(color == Color.GREY){
			return this;
		}
		else if(color == Color.WHITE){
			// on évite les cas limites
			if( checkAmbiguousAngleHorizontal(p) ){
				if(p.getX() < Main.X_YELLOW_LINE){
					return Main.getArea(1);
				}
				else if(p.getX() > Main.X_YELLOW_LINE && p.getX() < Main.X_BLACK_LINE){
					return Main.getArea(2);
				}
				else if(p.getX() > Main.X_BLACK_LINE && p.getX() < Main.X_RED_LINE){
					return Main.getArea(3);
				}
				else if(p.getX() > Main.X_RED_LINE){
					return Main.getArea(4);
				}
			}
		}
		return Main.getArea(15); // default Area
	}
	
	@Override
	public float[] getBorder() {
		return new float[]{
			0,
			2000,
			2700,
			3000
		};
	}

}
