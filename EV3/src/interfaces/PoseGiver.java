package interfaces;

import lejos.robotics.navigation.Pose;

/**
 * Interface implémenté par un gestionnaire de position permettant au autre objet d'obtenir la position courrante 
 * et d'informer le gestionnaire de position de changement d'état intéressant.
 * @author paul.carretero, florent.chastagner
 */
public interface PoseGiver {
	/**
	 * @return une Pose représentant l'état (position + direction) du robot au moment de l'appel.
	 */
	public Pose getPosition();
	
	/**
	 * permet à un autre objet de définir la Pose courrante.
	 * @param p une nouvelle Pose.
	 * @param updateArea Vrai si l'on doit forcer la mise à jour des Area, faux sinon
	 * @see Pose
	 */
	public void setPose(Pose p, boolean updateArea);
}
