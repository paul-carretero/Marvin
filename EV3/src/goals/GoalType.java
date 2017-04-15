package goals;

/**
 * permet d'identifier les Goal de manière précise
 */
public enum GoalType {
	/**
	 * GoalDrop (drop un palet)
	 */
	DROP,
	/**
	 * GoalGoToPosition (va une position)
	 */
	GO_TO_POSITION,
	/**
	 * GoalGrabAndDrop ( recherche un palet, le grab et de dépose dans les but adverse)
	 */
	GRAB_AND_DROP,
	/**
	 * GoalGrabOptimist (pas de recherche du meilleur angle)
	 */
	GRAB_OPTIMISTE,
	/**
	 * GoalGrabPessimist (Recherche du meilleur angle)
	 */
	GRAB_PESSIMISTE,
	/**
	 * GoalPlay (lance la génération d'objectif)
	 */
	PLAY,
	/**
	 * GoalRecalibrate (recalibre la pose du robot)
	 */
	RECALIBRATE,
	/**
	 * GoalIntercept (se positionne de manière à géner le robot ennemi)
	 */
	INTERCEPT;
}
