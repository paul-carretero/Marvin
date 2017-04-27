package interfaces;

/**
 * Interface notament impl�ment� par le gestionnaire de l'IA et des Objectifs.
 * Permet d'�tre informer lors d'ev�nement interne ou externe au syst�me.
 * @author paul.carretero
 */
public interface SignalListener {
	
	/**
	 * Signal � l'IA que le robot est en situation de perte.
	 */
	public void signalLost();
	
	/**
	 * Signal � l'IA que le robot n'est plus en situation de perte.
	 */
	public void signalNoLost();
	
	/**
	 * Signal � l'IA que le moteur principale est probablement bloqu�/marche � l'infini
	 */
	public void signalStalled();
	
	/**
	 * Signal � l'IA qu'un obstacle a �t� detect� par le radar
	 */
	public void signalObstacle();
	
	/**
	 * Signal � l'IA qu'un palet � �t� detct� par le capteur de pression
	 */
	public void signalPression();
	
	/**
	 * Signal � l'IA qu'un arr�t du programme est demand� par l'utilisateur.
	 */
	public void signalStop();
	
}
