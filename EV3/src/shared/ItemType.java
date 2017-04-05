package shared;

/**
 * Enumeration fournissant les possibilité pour le type d'un item
 * @see Item
 */
public enum ItemType {
	/**
	 * un palet
	 */
	PALET,
	/**
	 * l'item considéré comme le robot
	 */
	ME,
	/**
	 * un item dont on ne connait pas sa nature exacte
	 */
	UNDEFINED
}
