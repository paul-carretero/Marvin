package shared;

/**
 * Couple de IntPoint, non modifiable une fois instanciée
 * @see IntPoint
 */

public class Couple {

	/**
	 * second IntPoint du couple
	 */
	private final IntPoint first;
	/**
	 * premier IntPoint du couple
	 */
	private final IntPoint second;

	/**
	 * retourne une instance d'un couple de deux IntPoint
	 * @param first premier IntPoint du couple
	 * @param second second IntPoint du couple
	 */
	public Couple(IntPoint first, IntPoint second) {
	    this.first	= first;
		this.second	= second;
	}

	/**
	 * @return retourne le premier IntPoint du couple
	 */
	public IntPoint getfirst() { 
		return this.first; 
	}
	 
	/**
	 * @return retourne le second IntPoint du couple
	 */
	public IntPoint getsecond() { 
		return this.second;
	}
}
	