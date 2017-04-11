package interfaces;

/**
 * Propose à d'autre Thread d'attendre sur un Thread particulier, permet une meilleur gestion des attentes/notifications.
 */
public interface WaitProvider {
	/**
	 * @param ms une durée en millisecondes
	 */
	public void syncWait(int ms);
}
