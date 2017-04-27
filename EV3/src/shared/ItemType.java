package shared;

/**
 * Enumeration fournissant les possibilités pour le type d'un item
 * @see Item
 * @author paul.carretero
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
