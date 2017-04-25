package area;

import aiPlanner.Main;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Founi une représentation de l'Area centrale du terrain, délimitée par les 4 lignes rouge, verte, jaune et bleu
 */
public class CenterArea extends Area {

	/**
	 * @param id ID de l'area (6)
	 */
	public CenterArea(final int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(final Pose p) {
		return (
				p.getX() < (Main.X_RED_LINE + MARGE_ERREUR) && 
				p.getX() > (Main.X_YELLOW_LINE - MARGE_ERREUR) &&
				p.getY() > (Main.Y_GREEN_LINE - MARGE_ERREUR) &&
				p.getY() < (Main.Y_BLUE_LINE + MARGE_ERREUR)
		);
	}

	@Override
	public Area colorChange(final Color color, final float h) {
		switch (color) {
		case GREY:
			return this;
		case BLACK:
			return this;
		default:
			return Main.getArea(15);
		}
	}
	
	@Override
	public float[] getBorder() {
		return new float[]{
			500,
			1500,
			900,
			2100,
		};
	}
}
