package shared;

/**
 * réprésente les signaux primitif que peuvent envoyer les contrôleurs vers le thread principal
 * @see aiPlanner
 * @see eventManager
 * @see positionManager
 */
public enum SignalType {
	/**
	 * signal informant que le capteur de pression à détecter un item, probablement un palet
	 */
	PRESSION_PUSHED,
	
	/**
	 * signal informant que le capteur de pression ne détecte plus de palet
	 */
	PRESSION_RELEASED,
	
	/**
	 * signal informant que le calculateur de position est en erreur, la position fournit est invalide
	 */
	LOST, 
	
	/**
	 * signal utilisateur demandant l'arret du programme
	 */
	STOP, 
	
	/**
	 * signal informant que le moteur est bloqué et/ou tourne à l'infini
	 */
	STALLED_ENGINE, 
	
	/**
	 * signal informant que le robot n'a pas bougé sur pendant une longue periode. Il s'agit probablement d'une anomalie.
	 */
	BLOCKED, 
	
	/**
	 * signal informant que la position à été recalibrée
	 */
	NO_LOST, 
	
	/**
	 * signal informant qu'un obstacle (mur ou ennemi) à été détecté
	 */
	OBSTACLE;
}
