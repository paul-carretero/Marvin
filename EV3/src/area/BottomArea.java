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
	public BottomArea(final int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(final Pose p) {
		return (p.getY() < (Main.Y_BOTTOM_WHITE + MARGE_ERREUR));
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
			0,
			300
		};
	}
}
