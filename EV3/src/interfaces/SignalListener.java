package interfaces;

/**
 * Interface notament implémenté par le gestionnaire de l'IA et des Objectifs.
 * Permet d'être informer lors d'evénement interne ou externe au système.
 * @author paul.carretero
 */
public interface SignalListener {
	
	/**
	 * Signal à l'IA que le robot est en situation de perte.
	 */
	public void signalLost();
	
	/**
	 * Signal à l'IA que le robot n'est plus en situation de perte.
	 */
	public void signalNoLost();
	
	/**
	 * Signal à l'IA que le moteur principale est probablement bloqué/marche à l'infini
	 */
	public void signalStalled();
	
	/**
	 * Signal à l'IA qu'un obstacle a été detecté par le radar
	 */
	public void signalObstacle();
	
	/**
	 * Signal à l'IA qu'un palet à été detcté par le capteur de pression
	 */
	public void signalPression();
	
	/**
	 * Signal à l'IA qu'un arrêt du programme est demandé par l'utilisateur.
	 */
	public void signalStop();
	
}
