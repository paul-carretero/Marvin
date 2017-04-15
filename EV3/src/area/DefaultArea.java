package area;

import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Area par défaut, ne permet pas de vérifier la cohérence de la position
 */
public class DefaultArea extends Area {

	/**
	 * @param id ID de l'area (15)
	 */
	public DefaultArea(int id) {
		super(id);
	}

	@Override
	public boolean getConsistency(Pose p) {
		return true;
	}

	@Override
	public Area colorChange(Color color, Pose p) {
		return this;
	}
	
	@Override
	public float[] getBorder() {
		return new float[]{
			0,
			2000,
			0,
			3000
		};
	}

}
