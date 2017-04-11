package interfaces;

/**
 * Implémenté par le radar afin de proposer des primitives pour la detection d'objets.
 */
public interface DistanceGiver {
	/**
	 * @return un entier correspondant à la distance entre le centre de position du robot (entre les roues) et l'item le plus proche à porté radar
	 */
	public int getRadarDistance();
	
	/**
	 * @return vrai si le radar détecte quelque chose à porté, faux sinon
	 */
	public boolean checkSomething();
}
