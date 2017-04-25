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
	public Area colorChange(final Color color, final float h) {
		if(color == Color.GREY){
			return this;
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
