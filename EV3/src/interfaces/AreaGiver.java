package interfaces;

import area.Area;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Impl�ment� par le gestionnaire d'Area notament, propose des primitives pour la consultation de l'area courrante
 * @see Area
 * @author paul.carretero
 */
public interface AreaGiver {
	/**
	 * Informe le gestionnaire d'Area qu'il peut tenter de mettre � jour l'area courrante en fonction de la position actuelle.
	 * @param force force la mise � jour des areas avec la derni�re pose, si faux alors ne met pas � jour si area coh�rente
	 */
	public void updateArea(boolean force);
	
	/**
	 * Met � jour la pose p avec les donn�es des Areas
	 * @param p Une pose du robot
	 */
	public void updatePose(Pose p);
	
	/**
	 * @return retourne la couleur courrante
	 */
	public Color getColor();
	
	/**
	 * @return la couleur de la derni�re ligne de couleur traversee
	 */
	public Color getLastLine();
}
