package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Représente l'area en "haut" (Y entre 2700 et 3000) du terrain.
 * Les ID possible sont 0
 */

public class TopArea extends Area{
	
	/**
	 * @param id de l'area (14)
	 */
	public TopArea(final int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(final Pose p) {
		return (p.getY() > (Main.Y_TOP_WHITE - MARGE_ERREUR));
	}

	@Override
	public Area colorChange(final Color color, final Pose p) {
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
