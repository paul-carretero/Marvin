package interfaces;

import shared.Item;

import java.util.List;

import shared.Color;
import shared.IntPoint;

/**
 * Interface proposant la gestion des item (recherche) en se basant sur les données de la caméra
 * @author paul.carretero, florent.chastagner
 */
public interface ItemGiver {
	/**
	 * @return un item correspondant au palet le plus près de la position du robot
	 */
	public Item getNearestpalet();
	/**
	 * @return un item correspondant à la position du robot sur la carte
	 */
	public Item getMarvinPosition();
	/**
	 * @param searchPoint un point à partir duquel cherche un item
	 * @return l'item le plus prés du point searchPoint
	 */
	public Item getNearestItem(IntPoint searchPoint);
	/**
	 * @param position un point à partir duquel cherche un palet
	 * @return vrai si un palet existe proche de ce point, faux sinon
	 */
	public boolean checkpalet(IntPoint position);
	/**
	 * @param color la couleur d'une ligne significative (rouge, jaune, verte ou bleu)
	 * @return la liste des point sur cette ligne de couleur
	 */
	public List<IntPoint> searchPosition(Color color);
	/**
	 * @param start un point de départ pour chercher des position d'item
	 * @param minRange distance minimum de recherche a partir du point start
	 * @param maxRange distance maximum de recherche a partir du point start
	 * @return une liste contenant les item situé entre minRange et maxRange du point start
	 */
	public List<IntPoint> searchPosition(IntPoint start, int minRange, int maxRange);
	
	/**
	 * @return vrai si il reste des palet sur la map, faux sinon
	 */
	public boolean canPlayAgain();
}
