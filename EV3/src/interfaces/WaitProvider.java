package interfaces;

/**
 * Propose � d'autre Thread d'attendre sur un Thread particulier, permet une meilleur gestion des attentes/notifications.
 * @author paul.carretero, florent.chastagner
 */
public interface WaitProvider {
	/**
	 * @param ms une dur�e en millisecondes
	 */
	public void syncWait(int ms);
}
