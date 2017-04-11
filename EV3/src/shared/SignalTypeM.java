package shared;

/**
 * r�pr�sente les signaux primitif que peuvent envoyer les contr�leurs vers le thread principal
 * @see aiPlanner
 * @see eventManager
 * @see positionManager
 */
public enum SignalType {
	/**
	 * signal informant que le capteur de pression � d�tecter un item, probablement un palet
	 */
	PRESSION_PUSHED,
	
	/**
	 * signal informant que le capteur de pression ne d�tecte plus de palet
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
	 * signal informant que le moteur est bloqu� et/ou tourne � l'infini
	 */
	STALLED_ENGINE, 
	
	/**
	 * signal informant que le robot n'a pas boug� sur pendant une longue periode. Il s'agit probablement d'une anomalie.
	 */
	BLOCKED, 
	
	/**
	 * signal informant que la position � �t� recalibr�e
	 */
	NO_LOST, 
	
	/**
	 * signal informant qu'un obstacle (mur ou ennemi) � �t� d�tect�
	 */
	OBSTACLE;
}
