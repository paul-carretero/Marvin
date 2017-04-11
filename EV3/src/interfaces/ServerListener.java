package interfaces;

import java.util.List;

import shared.Item;

/**
 * Interface implémenté par un objet souhaitant recevoir des données structurés du serveur (de la caméra infrarouge du terrain).
 */
public interface ServerListener {
	/**
	 * Permet de recevoir la liste des Item du serveur.
	 * @param timer le moment en fonction du début du programme ou les Item on été récupéré par le serveur
	 * @param lastPointsReceived une List d'Item indéfini qui on été récupéré par le serveur.
	 */
	public void receiveRawPoints(int timer, List<Item> lastPointsReceived);
}
