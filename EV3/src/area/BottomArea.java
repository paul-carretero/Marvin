package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Représente l'area en "bas" (Y entre 0 et 300) du terrain.
 * Les ID possible sont 14
 */
public class BottomArea extends Area{
	
	/**
	 * @param id ID de l'area
	 */
	public BottomArea(int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(Pose p) {
		return (p.getY() < (Main.Y_BOTTOM_WHITE + MARGE_ERREUR));
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
					return Main.getArea(10);
				}
				if(p.getX() > Main.X_YELLOW_LINE && p.getX() < Main.X_BLACK_LINE){
					return Main.getArea(11);
				}
				if(p.getX() > Main.X_BLACK_LINE && p.getX() < Main.X_RED_LINE){
					return Main.getArea(12);
				}
				if(p.getX() > Main.X_RED_LINE){
					return Main.getArea(13);
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
			0,
			300
		};
	}

}
