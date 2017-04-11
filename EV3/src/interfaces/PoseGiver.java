package interfaces;

import lejos.robotics.navigation.Pose;

/**
 * Interface implémenté par un gestionnaire de position permettant au autre objet d'obtenir la position courrante 
 * et d'informer le gestionnaire de position de changement d'état intéressant.
 */
public interface PoseGiver {
	/**
	 * @return une Pose représentant l'état (position + direction) du robot au moment de l'appel.
	 */
	public Pose getPosition();
	
	/**
	 * Met à jour la position en considérant un X détecté de manière certaine.
	 * @param x une coordonnée x sûre.
	 */
	public void sendFixX(int x);
	
	/**
	 * Met à jour la position en considérant un X détecté de manière certaine.
	 * @param y une coordonnée y sûre.
	 */
	public void sendFixY(int y);
	
	/**
	 * @return l'ID de l'aire courante basé sur le gestionnaire d'Area
	 */
	public int getAreaId();
	
	/**
	 * permet à un autre objet de définir la Pose courrante.
	 * @param p une nouvelle Pose.
	 * @see Pose
	 */
	public void setPose(Pose p);
}
