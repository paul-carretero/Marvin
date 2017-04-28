package interfaces;

import java.util.List;

import shared.Item;

/**
 * Interface impl�ment�e par un objet souhaitant recevoir des donn�es structur�s du serveur (de la cam�ra infrarouge du terrain).
 * @author paul.carretero, florent.chastagner
 */
public interface ServerListener {
	/**
	 * Permet de recevoir la liste des Item du serveur.
	 * @param timer le moment en fonction du d�but du programme ou les Item on �t� r�cup�r� par le serveur
	 * @param lastPointsReceived une List d'Item ind�fini qui on �t� r�cup�r� par le serveur.
	 */
	public void receiveRawPoints(int timer, List<Item> lastPointsReceived);
}
