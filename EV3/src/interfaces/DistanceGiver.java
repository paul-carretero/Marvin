package interfaces;

/**
 * Impl�ment� par le radar afin de proposer des primitives pour la detection d'objets.
 */
public interface DistanceGiver {
	/**
	 * @return un entier correspondant � la distance entre le centre de position du robot (entre les roues) et l'item le plus proche � port� radar
	 */
	public int getRadarDistance();
	
	/**
	 * @return vrai si le radar d�tecte quelque chose � port�, faux sinon
	 */
	public boolean checkSomething();
}
