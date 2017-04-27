package interfaces;

import area.Area;
import lejos.robotics.navigation.Pose;
import shared.Color;

/**
 * Implémenté par le gestionnaire d'Area notament, propose des primitives pour la consultation de l'area courrante
 * @see Area
 * @author paul.carretero
 */
public interface AreaGiver {
	/**
	 * Informe le gestionnaire d'Area qu'il peut tenter de mettre à jour l'area courrante en fonction de la position actuelle.
	 * @param force force la mise à jour des areas avec la dernière pose, si faux alors ne met pas à jour si area cohérente
	 */
	public void updateArea(boolean force);
	
	/**
	 * Met à jour la pose p avec les données des Areas
	 * @param p Une pose du robot
	 */
	public void updatePose(Pose p);
	
	/**
	 * @return retourne la couleur courrante
	 */
	public Color getColor();
	
	/**
	 * @return la couleur de la dernière ligne de couleur traversee
	 */
	public Color getLastLine();
}
