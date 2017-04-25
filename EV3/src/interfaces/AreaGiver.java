package interfaces;

import area.Area;
import shared.Color;

/**
 * Impl�ment� par le gestionnaire d'Area notament, propose des primitives pour la consultation de l'area courrante
 * @see Area
 */
public interface AreaGiver {
	/**
	 * @return une Area correspondant � l'Area courrante
	 */
	public Area getCurrentArea();
	
	/**
	 * Informe le gestionnaire d'Area qu'il peut tenter de mettre � jour l'area courrante en fonction de la position actuelle.
	 */
	public void updateArea();
	
	/**
	 * @return retourne la couleur courrante
	 */
	public Color getColor();
}
