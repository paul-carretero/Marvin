package shared;

/**
 * Enumeration fournissant les possibilit�s pour le type d'un item
 * @see Item
 * @author paul.carretero
 */
public enum ItemType {
	/**
	 * un palet
	 */
	PALET,
	/**
	 * l'item consid�r� comme le robot
	 */
	ME,
	/**
	 * un item dont on ne connait pas sa nature exacte
	 */
	UNDEFINED
}
